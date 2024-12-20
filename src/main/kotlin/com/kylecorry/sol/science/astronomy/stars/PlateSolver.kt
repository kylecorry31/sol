package com.kylecorry.sol.science.astronomy.stars

import com.kylecorry.sol.math.analysis.Trigonometry
import com.kylecorry.sol.math.sumOfFloat
import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.time.Time
import com.kylecorry.sol.units.Coordinate
import java.time.ZonedDateTime
import kotlin.math.absoluteValue

internal class PlateSolver(
    private val tolerance: Float = 0.02f,
    private val minMatches: Int = 5,
    private val numNeighbors: Int = 3
) {

    fun solve(
        readings: List<AltitudeAzimuth>,
        time: ZonedDateTime,
        approximateLocation: Coordinate = Time.getLocationFromTimeZone(time.zone)
    ): List<Pair<AltitudeAzimuth, Star>> {
        if (readings.size < numNeighbors + 1) {
            // Not enough readings to solve
            return emptyList()
        }

        // Step 1: Get quads from the readings
        val readingsQuads = getQuads(readings)

        // Step 2: Get the quads from the star catalog
        val catalogQuads = getAllQuads(Star.entries, time, approximateLocation)

        // Step 3: Match the star quads
        val matches = mutableListOf<Pair<AltitudeAzimuth, Star>>()
        for (reading in readingsQuads) {
            val minDistanceMatch = catalogQuads.minBy {
                reading.second.zip(it.second.second).sumOfFloat { (a, b) ->
                    (a - b).absoluteValue
                }
            }

            val count = reading.second.zip(minDistanceMatch.second.second).count { (a, b) ->
                (a - b).absoluteValue < tolerance
            }

            if (count >= minMatches) {
                matches.add(Pair(reading.first, minDistanceMatch.first))
            }
        }

        return matches
    }

    private fun getAllQuads(
        stars: List<Star>,
        time: ZonedDateTime,
        approximateLocation: Coordinate
    ): List<Pair<Star, Pair<AltitudeAzimuth, FloatArray>>> {
        val degreesOfSeparation = 1f
        val degreesStep = 0.1f
        var currentSeparation = 0f

        // Remove stars that are way below the horizon
        // TODO: Also remove stars that are way out of sight based on the input readings
        var starReadings = stars.map {
            it to AltitudeAzimuth(
                Astronomy.getStarAltitude(it, time, approximateLocation, true),
                Astronomy.getStarAzimuth(it, time, approximateLocation).value
            )
        }.filter { it.second.altitude > -10 }

        val quads = mutableListOf<Pair<Star, Pair<AltitudeAzimuth, FloatArray>>>()
        while (currentSeparation <= degreesOfSeparation) {
            quads.addAll(
                starReadings.map { it.first }.zip(
                    getQuads(
                        starReadings.map { it.second },
                        currentSeparation,
                        currentSeparation + 60f
                    )
                )
            )
            currentSeparation += degreesStep
        }

        return quads.distinct()
    }

    private fun getQuads(
        readings: List<AltitudeAzimuth>,
        minSeparation: Float = 0f, // Minimum angular separation in degrees
        maxSeparation: Float = 180f // Maximum angular separation in degrees
    ): List<Pair<AltitudeAzimuth, FloatArray>> {
        val quads = mutableListOf<Pair<AltitudeAzimuth, FloatArray>>()
        for (i in readings.indices) {
            val reading = readings[i]

            // Find all neighbors within the specified angular separation range
            val neighbors = readings.mapIndexedNotNull { j, neighbor ->
                if (i != j) {
                    val distance = Trigonometry.angularDistance(
                        reading.azimuth,
                        reading.altitude,
                        neighbor.azimuth,
                        neighbor.altitude
                    )
                    if (distance in minSeparation..maxSeparation) {
                        Pair(neighbor, distance)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }.sortedBy { it.second }

            val quad = listOf(reading) + neighbors.take(numNeighbors).map { it.first }

            // Calculate the distances between each pair in the quad
            val distances = mutableListOf<Float>()
            for (j in quad.indices) {
                for (k in j + 1 until quad.size) {
                    distances.add(
                        Trigonometry.angularDistance(
                            quad[j].azimuth,
                            quad[j].altitude,
                            quad[k].azimuth,
                            quad[k].altitude
                        )
                    )
                }
            }

            // Get the maximum distance in the quad
            val maxDistance = distances.maxOrNull() ?: 0f

            // Normalize the distances
            val normalizedDistances = distances.map { it / maxDistance }.toFloatArray()

            quads.add(Pair(reading, normalizedDistances))
        }
        return quads
    }


}