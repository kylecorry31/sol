package com.kylecorry.sol.science.astronomy.stars

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.optimization.SimulatedAnnealingOptimizer
import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.science.astronomy.Astronomy.getStarAltitude
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.science.geography.Geography
import com.kylecorry.sol.science.geology.Geofence
import com.kylecorry.sol.time.Time
import com.kylecorry.sol.units.Coordinate

internal class StarLocationCalculator {

    fun getLocationFromStars(
        starReadings: List<StarAltitudeReading>,
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
        while (step > 0.0001) {
            val optimizer =
                SimulatedAnnealingOptimizer(1000.0, stepSize = step, maxIterations = 200, initialValue = Pair(lon, lat))
            var weights = starReadings.map {
                1 / square(90.0 - it.altitude)
            }
            val totalWeight = weights.sum()
            weights = weights.map { it / totalWeight }

            val result = optimizer.optimize(
                Range(lon - step * 2, lon + step * 2),
                Range(constrainLatitude(lat - step * 6), constrainLatitude(lat + step * 6)),
                false,
                { lon, lat ->
                    starReadings.mapIndexed { i, reading ->
                        val expectedAltitude = getStarAltitude(
                            reading.star,
                            reading.time,
                            Coordinate(lat, lon),
                            true
                        )
                        square(
                            reading.altitude + (if (adjustForAltitudeBias) (bias
                                ?: 0f) else 0f) - expectedAltitude.toDouble()
                        ) * weights[i]
                    }.sum()
                }
            )
            lat = result.second
            lon = result.first
            step *= 0.5
        }

        return Coordinate.constrained(lat, lon)
    }

    private fun triangulateApproximateLocation(
        starReadings: List<StarAltitudeReading>,
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