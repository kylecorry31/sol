package com.kylecorry.sol.science.meteorology.clouds

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.units.Reading
import java.time.Duration
import java.time.Instant

internal class CloudPrecipitationCalculator {

    fun getPrecipitationTime(clouds: List<Reading<CloudGenus?>>): Range<Instant>? {
        // Try to only include clouds which are contributing to the precipitation forecast
        val pattern = frontPatterns.firstOrNull {
            getMatch(clouds, it) != null
        }

        val cloudsToConsider = pattern?.let { getMatch(clouds, it) } ?: clouds

        val precipitationRanges = cloudsToConsider.map {
            val timeRange = getCloudPrecipitationTimeRange(it.value) ?: return@map null
            Range(it.time.plus(timeRange.start), it.time.plus(timeRange.end)) to it.time
        }.filterNotNull().sortedByDescending { it.second }.map { it.first }

        return Range.intersection(precipitationRanges, stopWhenNoIntersection = true)
    }

    fun getMatch(
        clouds: List<Reading<CloudGenus?>>,
        pattern: List<List<CloudGenus?>>,
        requireExact: Boolean = false
    ): List<Reading<CloudGenus?>>? {
        val reversedClouds = clouds.sortedByDescending { it.time }
        val reversedPattern = pattern.reversed()

        var patternIdx = 0
        var foundCurrentPattern = false
        val matches = mutableListOf<Reading<CloudGenus?>>()

        for (i in reversedClouds.indices) {
            val cloud = reversedClouds[i]
            val currentPattern = reversedPattern[patternIdx]
            val nextPattern = reversedPattern.getOrNull(patternIdx + 1)
            if (!foundCurrentPattern && currentPattern.contains(cloud.value)) {
                // This is the first hit against the current pattern
                foundCurrentPattern = true
                matches.add(cloud)
            } else if (foundCurrentPattern && nextPattern != null && nextPattern.contains(cloud.value)) {
                // This is the first hit against the next pattern
                patternIdx++
                foundCurrentPattern = true
                matches.add(cloud)
            } else if (foundCurrentPattern && currentPattern.contains(cloud.value)) {
                // This is a continued hit against the current pattern
                matches.add(cloud)
            } else {
                // This cloud does not match the current pattern
                if (requireExact) {
                    return null
                }
            }
        }

        if (foundCurrentPattern && patternIdx == reversedPattern.size - 1) {
            return matches.reversed()
        }

        return null
    }

    private fun getCloudPrecipitationTimeRange(cloud: CloudGenus?): Range<Duration>? {
        return when (cloud) {
            CloudGenus.Cirrus -> Range(Duration.ofHours(12), Duration.ofHours(24))
            CloudGenus.Cirrocumulus -> Range(Duration.ofHours(8), Duration.ofHours(12))
            CloudGenus.Cirrostratus -> Range(Duration.ofHours(10), Duration.ofHours(15))
            CloudGenus.Altocumulus -> Range(Duration.ZERO, Duration.ofHours(12))
            CloudGenus.Altostratus -> Range(Duration.ZERO, Duration.ofHours(8))
            CloudGenus.Nimbostratus, CloudGenus.Cumulonimbus -> Range(Duration.ZERO, Duration.ZERO)
            CloudGenus.Stratus, CloudGenus.Cumulus -> Range(Duration.ZERO, Duration.ofHours(3))
            CloudGenus.Stratocumulus, null -> null
        }
    }

    companion object {
        private val cirro =
            listOf(CloudGenus.Cirrus, CloudGenus.Cirrocumulus, CloudGenus.Cirrostratus)
        private val alto = listOf(CloudGenus.Altocumulus, CloudGenus.Altostratus)
        private val warm = listOf(CloudGenus.Stratus, CloudGenus.Nimbostratus)
        private val cold = listOf(CloudGenus.Cumulus, CloudGenus.Cumulonimbus)
        private val storm = listOf(CloudGenus.Nimbostratus, CloudGenus.Cumulonimbus)
        private val coldStorm = listOf(CloudGenus.Cumulonimbus)
        private val warmStorm = listOf(CloudGenus.Nimbostratus)

        val frontPatterns = listOf(
            listOf(cirro, alto, warm),
            listOf(cirro, alto, cold),
            listOf(storm),
            listOf(cirro, alto)
        )

        val coldFrontPatterns = listOf(
            listOf(cirro, alto, cold),
            listOf(coldStorm)
        )

        val warmFrontPatterns = listOf(
            listOf(cirro, alto, warm),
            listOf(warmStorm)
        )
    }

}