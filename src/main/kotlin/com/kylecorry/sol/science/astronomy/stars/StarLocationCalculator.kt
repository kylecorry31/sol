package com.kylecorry.sol.science.astronomy.stars

import com.kylecorry.sol.math.MathExtensions.toRadians
import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.Vector
import com.kylecorry.sol.math.algebra.LinearAlgebra
import com.kylecorry.sol.math.algebra.Matrix
import com.kylecorry.sol.math.optimization.ConvergenceOptimizer
import com.kylecorry.sol.math.optimization.LeastSquaresOptimizer
import com.kylecorry.sol.math.optimization.SimulatedAnnealingOptimizer
import com.kylecorry.sol.math.statistics.Statistics
import com.kylecorry.sol.math.trigonometry.Trigonometry.deltaAngle
import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.science.geography.Geography
import com.kylecorry.sol.science.geology.Geofence
import com.kylecorry.sol.time.Time
import com.kylecorry.sol.units.Coordinate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
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
            return getLocationFromStarsAltitudeOnly(starReadings, approximateLocation)
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

            val location = getLocationFromStarsObservations(newStars, approximateLocation)

            newStars.mapIndexed { i, reading ->
                val expectedPosition = Astronomy.getStarPosition(
                    reading.star,
                    reading.time,
                    location,
                    true
                )
                loss(reading.altitude - expectedPosition.altitude) +
                        loss(deltaAngle(checkNotNull(reading.azimuth), expectedPosition.azimuth.value))
            }.sum()
        }

        return getLocationFromStarsObservations(starReadings.map {
            StarReading(
                it.star,
                it.altitude + altitudeBias.toFloat(),
                it.azimuth?.plus(azimuthBias.toFloat()),
                it.time
            )
        }, approximateLocation)
    }

    private fun getLocationFromStarsObservations(
        starReadings: List<StarReading>,
        approximateLocation: Coordinate?
    ): Coordinate {
        val timezoneLocation = Time.getLocationFromTimeZone(starReadings.first().time.zone)

        var lat = constrainLatitude(approximateLocation?.latitude ?: timezoneLocation.latitude)
        var lon = approximateLocation?.longitude ?: timezoneLocation.longitude

        for (ignored in 0..<20) {
            val expectedAltitudes = starReadings.map {
                Astronomy.getStarPosition(it.star, it.time, Coordinate(lat, lon), true).altitude
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
                floatArrayOf(-m, 1f) to b
            }

            // Solve using least squares
            val ls = LinearAlgebra.robustLeastSquares(
                Matrix.create(linesOfPosition.map { it.first }.toTypedArray()),
                Vector(linesOfPosition.map { it.second }.toFloatArray()),
                minScale = 0.05f
            )

            if (ls.norm() < 0.000001) {
                break
            }

            val newCoord = Coordinate(lat + ls[0].toDouble(), lon + ls[1].toDouble())
            lat = newCoord.latitude
            lon = newCoord.longitude
        }

        return Coordinate(lat, lon)
    }

    /**
     * Each altitude puts the location on a circle around the star's zenith point. The circles'
     * intersections (and the approximate location) seed a least squares fit of the altitudes, and
     * the best fit wins. Ties (ex. two stars) go to the solution closest to the approximate location.
     */
    private fun getLocationFromStarsAltitudeOnly(
        starReadings: List<StarReading>,
        approximateLocation: Coordinate?
    ): Coordinate? {
        if (starReadings.size <= 1) {
            return null
        }

        val reference = approximateLocation ?: Time.getLocationFromTimeZone(starReadings.first().time.zone)
        val seeds = getZenithCircleIntersections(starReadings) + reference
        val fits = seeds.map { seed ->
            val location = fitAltitudes(starReadings, seed)
            location to altitudeRms(starReadings, location)
        }
        val bestRms = fits.minOf { it.second }
        return fits.filter { it.second <= bestRms + RMS_TIE_TOLERANCE }
            .minByOrNull { it.first.distanceTo(reference) }?.first
    }

    private fun getZenithCircleIntersections(starReadings: List<StarReading>): List<Coordinate> {
        val zenithLocations = starReadings.map { reading ->
            val distance = Astronomy.getZenithDistance(reading.altitude)
            val coordinate = reading.star.coordinate.getZenithCoordinate(reading.time.toUniversalTime())
            Geofence(coordinate, distance)
        }
        return Geography.trilaterate(zenithLocations).locations
    }

    private fun fitAltitudes(starReadings: List<StarReading>, start: Coordinate): Coordinate {
        fun altitude(index: Int, guess: List<Float>): Float {
            val reading = starReadings[index]
            val location = Coordinate(
                constrainLatitude(guess[0].toDouble()),
                Coordinate.toLongitude(guess[1].toDouble())
            )
            return Astronomy.getStarPosition(reading.star, reading.time, location, true).altitude
        }

        val result = try {
            LeastSquaresOptimizer().optimize(
                starReadings.indices.map { listOf(it.toFloat()) },
                starReadings.map { it.altitude },
                maxIterations = MAX_FIT_ITERATIONS,
                dampingFactor = 1f,
                tolerance = FIT_TOLERANCE,
                initialValue = listOf(start.latitude.toFloat(), start.longitude.toFloat()),
                distanceFn = { point, guess -> altitude(point[0].toInt(), guess) },
                jacobianFn = { index, _, guess ->
                    val current = altitude(index, guess)
                    guess.indices.map { j ->
                        val stepped = guess.toMutableList().also { it[j] += JACOBIAN_STEP }
                        (altitude(index, stepped) - current) / JACOBIAN_STEP
                    }
                }
            )
        } catch (_: IllegalStateException) {
            return start
        }
        return Coordinate(constrainLatitude(result[0].toDouble()), Coordinate.toLongitude(result[1].toDouble()))
    }

    private fun altitudeResiduals(starReadings: List<StarReading>, lat: Double, lon: Double): DoubleArray {
        val location = Coordinate(constrainLatitude(lat), Coordinate.toLongitude(lon))
        return DoubleArray(starReadings.size) {
            val reading = starReadings[it]
            reading.altitude.toDouble() - Astronomy.getStarPosition(reading.star, reading.time, location, true).altitude
        }
    }

    private fun altitudeCost(starReadings: List<StarReading>, lat: Double, lon: Double): Double {
        return altitudeResiduals(starReadings, lat, lon).sumOf { it * it }
    }

    private fun altitudeRms(starReadings: List<StarReading>, location: Coordinate): Double {
        return sqrt(altitudeCost(starReadings, location.latitude, location.longitude) / starReadings.size)
    }

    private fun constrainLatitude(latitude: Double): Double {
        return latitude.coerceIn(-90.0, 90.0)
    }

    private fun loss(residual: Float, threshold: Float = 1f): Double {
        return Statistics.huberLoss(residual, threshold).toDouble()
    }

    companion object {
        private const val RMS_TIE_TOLERANCE = 0.001
        private const val MAX_FIT_ITERATIONS = 50
        private const val JACOBIAN_STEP = 1e-3f
        private const val FIT_TOLERANCE = 1e-6f
    }
}
