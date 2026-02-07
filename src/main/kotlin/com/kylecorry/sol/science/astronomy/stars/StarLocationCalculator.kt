package com.kylecorry.sol.science.astronomy.stars

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.SolMath.toRadians
import com.kylecorry.sol.math.Vector
import com.kylecorry.sol.math.algebra.LinearAlgebra
import com.kylecorry.sol.math.algebra.Matrix
import com.kylecorry.sol.math.analysis.Trigonometry.deltaAngle
import com.kylecorry.sol.math.arithmetic.Arithmetic.square
import com.kylecorry.sol.math.optimization.ConvergenceOptimizer
import com.kylecorry.sol.math.optimization.SimulatedAnnealingOptimizer
import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.science.astronomy.Astronomy.getStarAltitude
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.science.geography.Geography
import com.kylecorry.sol.science.geology.Geofence
import com.kylecorry.sol.time.Time
import com.kylecorry.sol.units.Coordinate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

internal class StarLocationCalculator {

    fun getLocationFromStars(
        starReadings: List<StarReading>,
        approximateLocation: Coordinate?
    ): Coordinate? {
        if (starReadings.size <= 1) {
            return null
        }

        // If there are no azimuth readings, delegate to the other method
        if (starReadings.any { it.azimuth == null } || starReadings.size == 2) {
            return getLocationFromStarsAltitudeOnly(starReadings, approximateLocation, false)
        }

        val optimizer = ConvergenceOptimizer(
            1f,
            0.0001f,
            0.0 to 0.0
        ) { stepSize, center ->
            SimulatedAnnealingOptimizer(
                1000.0,
                stepSize = stepSize.toDouble(),
                maxIterations = 200,
                initialValue = center
            )
        }

        val (azimuthBias, altitudeBias) = optimizer.optimize(
            Range(-20.0, 20.0),
            Range(-20.0, 20.0),
            false
        ) { azimuthBias, altitudeBias ->
            val newStars = starReadings.map {
                StarReading(
                    it.star,
                    it.altitude + altitudeBias.toFloat(),
                    it.azimuth?.plus(azimuthBias.toFloat()),
                    it.time
                )
            }

            val location = getLocationFromStarsAltitudeAzimuth(newStars, approximateLocation)

            newStars.mapIndexed { i, reading ->
                val expectedAltitude =
                    getStarAltitude(reading.star, reading.time, location, true)
                val expectedAzimuth = Astronomy.getStarAzimuth(
                    reading.star,
                    reading.time,
                    location
                ).value
                square(reading.altitude - expectedAltitude).toDouble() +
                        square(deltaAngle(reading.azimuth!!, expectedAzimuth)).toDouble()
            }.sum()
        }

        return getLocationFromStarsAltitudeAzimuth(starReadings.map {
            StarReading(
                it.star,
                it.altitude + altitudeBias.toFloat(),
                it.azimuth?.plus(azimuthBias.toFloat()),
                it.time
            )
        }, approximateLocation)
    }

    private fun getLocationFromStarsAltitudeAzimuth(
        starReadings: List<StarReading>,
        approximateLocation: Coordinate?
    ): Coordinate {
        val timezoneLocation = Time.getLocationFromTimeZone(starReadings.first().time.zone)

        var lat = constrainLatitude(approximateLocation?.latitude ?: timezoneLocation.latitude)
        var lon = approximateLocation?.longitude ?: timezoneLocation.longitude

        for (i in 0 until 20) {
            val expectedAltitudes = starReadings.map {
                getStarAltitude(it.star, it.time, Coordinate(lat, lon), true)
            }

            // Step 3: Determine lines of position
            val linesOfPosition = expectedAltitudes.mapIndexed { i, alt ->
                val distance = starReadings[i].altitude - alt
                val azimuth = starReadings[i].azimuth ?: 0f
                val lineAngle = azimuth + 90f
                val m = tan(lineAngle.toRadians())
                val pointX = cos(azimuth.toRadians()) * distance
                val pointY = sin(azimuth.toRadians()) * distance
                val b = pointY - m * pointX
                arrayOf(-m, 1f) to b
            }

            // Solve using least squares
            val ls = LinearAlgebra.leastSquares(
                Matrix.create(linesOfPosition.map { it.first }.toTypedArray()),
                Vector(linesOfPosition.map { it.second }.toFloatArray())
            )

            if (ls.norm() < 0.000001) {
                break
            }

            val newCoord = Coordinate(lat + ls[0].toDouble(), lon + ls[1].toDouble())
            lat = newCoord.latitude
            lon = newCoord.longitude
        }

        return Coordinate.constrained(lat, lon)
    }

    private fun getLocationFromStarsAltitudeOnly(
        starReadings: List<StarReading>,
        approximateLocation: Coordinate?,
        adjustForAltitudeBias: Boolean = false
    ): Coordinate? {
        if (starReadings.size <= 1) {
            return null
        }

        // Step 1: Determine an approximate location of the user using their timezone
        val timezoneLocation = Time.getLocationFromTimeZone(starReadings.first().time.zone)

        var step = 10.0
        var lat = constrainLatitude(approximateLocation?.latitude ?: timezoneLocation.latitude)
        var lon = approximateLocation?.longitude ?: timezoneLocation.longitude


        // Step 2: Refine the location using triangulation
        val (approximateLocation, bias) = triangulateApproximateLocation(starReadings, Coordinate.constrained(lat, lon))
        if (approximateLocation != null) {
            lat = approximateLocation.latitude
            lon = approximateLocation.longitude
            step = 1.0
        }

        // Step 3: Further refine the location using simulated annealing
        val optimizer = ConvergenceOptimizer(
            step.toFloat(),
            0.0001f,
            lon to lat,
        ) { stepSize, center ->
            SimulatedAnnealingOptimizer(
                1000.0,
                stepSize = stepSize.toDouble(),
                maxIterations = 200,
                initialValue = center
            )
        }


        var weights = starReadings.map {
            1 / square(90.0 - it.altitude)
        }
        val totalWeight = weights.sum()
        weights = weights.map { it / totalWeight }
        val result = optimizer.optimize(
            Range(lon - step * 2, lon + step * 2),
            Range(constrainLatitude(lat - step * 6), constrainLatitude(lat + step * 6)),
            false
        ) { lon, lat ->
            starReadings.mapIndexed { i, reading ->
                val expectedAltitude = getStarAltitude(
                    reading.star,
                    reading.time,
                    Coordinate.constrained(lat, lon),
                    true
                )
                square(
                    reading.altitude + (if (adjustForAltitudeBias) (bias
                        ?: 0f) else 0f) - expectedAltitude.toDouble()
                ) * weights[i]
            }.sum()
        }

        lat = result.second
        lon = result.first

        return Coordinate.constrained(lat, lon)
    }

    private fun triangulateApproximateLocation(
        starReadings: List<StarReading>,
        approximateLocation: Coordinate
    ): Pair<Coordinate?, Float?> {
        if (starReadings.size <= 1) {
            return null to null
        }

        val zenithLocations = starReadings.mapIndexed { index, reading ->
            val distance = Astronomy.getZenithDistance(reading.altitude)
            val coordinate = reading.star.coordinate.getZenithCoordinate(reading.time.toUniversalTime())
            Geofence(coordinate, distance)
        }
        val result = Geography.trilaterate(zenithLocations, calculateBias = true)
        if (result.locations.size <= 1) {
            return result.locations.firstOrNull() to result.biasDegrees
        }

        return result.locations.minByOrNull { it.distanceTo(approximateLocation) } to result.biasDegrees
    }

    private fun constrainLatitude(latitude: Double): Double {
        return latitude.coerceIn(-90.0, 90.0)
    }

}