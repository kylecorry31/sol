package com.kylecorry.sol.science.astronomy.stars

import com.kylecorry.sol.math.analysis.Trigonometry
import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.time.Time
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.time.ZonedDateTime
import kotlin.math.absoluteValue
import kotlin.math.max

internal class PlateSolver(
    private val tolerance: Float = 0.04f,
    private val minMatches: Int = 5,
    private val numNeighbors: Int = 3,
    private val minMagnitude: Float = 4f
) {

    fun solve(
        readings: List<AltitudeAzimuth>,
        time: ZonedDateTime,
        approximateLocation: Coordinate = Time.getLocationFromTimeZone(time.zone)
    ): List<DetectedStar> {
        if (readings.size < numNeighbors + 1) {
            // Not enough readings to solve
            return emptyList()
        }

        // Step 1: Get quads from the readings
        val readingsQuads = getQuads(readings)

        // Step 2: Get the quads from the star catalog
        val catalogQuads = getAllQuads(STAR_CATALOG, time, approximateLocation)

        // Step 3: Match the star quads
        val matches = mutableListOf<DetectedStar>()
        val queue = readingsQuads.toMutableList()
        while (queue.isNotEmpty()) {
            val reading = queue.removeAt(0)
            val possibleMatches = catalogQuads.map {
                it to getConfidence(reading.second, it.second.second)
            }.sortedByDescending { it.second }.take(6)

            // Go through each possible match and see if it is a better match than what is recorded
            var mostConfidentMatch: Pair<Star, Pair<AltitudeAzimuth, FloatArray>>? = null
            for (possibleMatch in possibleMatches) {
                val confidence = possibleMatch.second
                if (matches.none { it.star == possibleMatch.first.first } || confidence > matches.first { it.star == possibleMatch.first.first }.confidence) {
                    val existing = matches.filter { it.star == possibleMatch.first.first }
                    matches.removeAll(existing)
                    queue.addAll(readingsQuads.filter { q -> existing.any { q.first == it.reading } })
                    mostConfidentMatch = possibleMatch.first
                    break
                }
            }

            if (mostConfidentMatch == null) {
                continue
            }

            val count = reading.second.zip(mostConfidentMatch.second.second).count { (a, b) ->
                (a - b).absoluteValue < tolerance
            }

            if (count >= minMatches) {
                val confidence = getConfidence(reading.second, mostConfidentMatch.second.second)
                matches.add(
                    DetectedStar(
                        mostConfidentMatch.first,
                        reading.first,
                        confidence
                    )
                )
            }
        }

        return matches
    }

    private fun getAllQuads(
        stars: List<Star>,
        time: ZonedDateTime,
        approximateLocation: Coordinate
    ): List<Pair<Star, Pair<AltitudeAzimuth, FloatArray>>> {
        val degreesOfSeparation = 10f
        val degreesStep = 0.2f
        var currentSeparation = 0f

        // Remove stars that are way below the horizon
        // TODO: Also remove stars that are way out of sight based on the input readings
        // NOTE: Magnitude is inverted, so lower is brighter
        val starReadings = stars.filter { it.magnitude <= minMagnitude }.map {
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
                        currentSeparation + 80f
                    )
                )
            )
            currentSeparation += degreesStep
        }

        // Remove duplicates (based on the start and distance array)
        return quads.distinctBy { it.first to it.second.second.toList() }.toList()
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

    private fun getConfidence(v1: FloatArray, v2: FloatArray): Float {
        val percentDifferences = v1.zip(v2).map { (a, b) ->
            (a - b).absoluteValue / max(a, b)
        }

        return 1 - percentDifferences.max()
    }
}