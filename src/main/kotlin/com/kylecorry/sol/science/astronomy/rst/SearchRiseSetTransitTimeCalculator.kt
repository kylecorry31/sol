package com.kylecorry.sol.science.astronomy.rst

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.astronomy.AstroUtils
import com.kylecorry.sol.science.astronomy.RiseSetTransitTimes
import com.kylecorry.sol.science.astronomy.locators.ICelestialLocator
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.time.Time.atStartOfDay
import com.kylecorry.sol.time.Time.middle
import com.kylecorry.sol.units.Coordinate
import java.time.ZonedDateTime

internal class SearchRiseSetTransitTimeCalculator : IRiseSetTransitTimeCalculator {
    override fun calculate(
        locator: ICelestialLocator,
        date: ZonedDateTime,
        location: Coordinate,
        standardAltitude: Double,
        withRefraction: Boolean,
        withParallax: Boolean
    ): RiseSetTransitTimes {
        var time = date.atStartOfDay()
        val currentDate = date.toLocalDate()
        val parameters = SearchParameters(
            locator,
            location,
            standardAltitude,
            withRefraction,
            withParallax
        )
        // Initialize the first altitude
        val yesterdayAltitude = getAltitude(parameters, time.minusHours(1))
        var lastAltitude = getAltitude(parameters, time)
        var isRising = lastAltitude > yesterdayAltitude

        var setTimeRange: Range<ZonedDateTime>? = null
        var riseTimeRange: Range<ZonedDateTime>? = null
        var transitTimeRange: Range<ZonedDateTime>? = null

        // Find the hour of each event
        while (time.toLocalDate() == currentDate) {
            val altitude = getAltitude(parameters, time)

            // Check if it just set past the standard altitude
            if (lastAltitude >= standardAltitude && altitude < standardAltitude) {
                setTimeRange = Range(time.minusHours(1), time)
            }

            // Check if it just rose past the standard altitude
            if (lastAltitude <= standardAltitude && altitude > standardAltitude) {
                riseTimeRange = Range(time.minusHours(1), time)
            }

            // Check if it just crossed transit
            if (transitTimeRange == null){
                val isCurrentlyRising = altitude > getAltitude(parameters, time.minusMinutes(1))
                if (isRising && !isCurrentlyRising && altitude >= standardAltitude) {
                    transitTimeRange = Range(time.minusHours(1), time)
                }
                isRising = isCurrentlyRising
            }

            // If all three times are found, break
            if (setTimeRange != null && riseTimeRange != null && transitTimeRange != null) {
                break
            }

            lastAltitude = altitude
            time = time.plusHours(1)
        }

        // Narrow down the times
        val rise = riseTimeRange?.let { getRiseTime(parameters, it) }
        val set = setTimeRange?.let { getSetTime(parameters, it) }
        val transit = transitTimeRange?.let { getTransitTime(parameters, it) }

        return RiseSetTransitTimes(rise, transit, set)
    }

    private fun getRiseTime(parameters: SearchParameters, range: Range<ZonedDateTime>): ZonedDateTime {
        // Conduct a binary search to find the rise time, with a max of 100 iterations
        var time = range.start
        var end = range.end
        var iterations = 0
        while (time < end && iterations < 100) {
            val mid = Range(time, end).middle()
            val altitude = getAltitude(parameters, mid)
            if (altitude > parameters.standardAltitude) {
                end = mid
            } else {
                time = mid
            }
            iterations++
        }

        return time
    }

    private fun getSetTime(parameters: SearchParameters, range: Range<ZonedDateTime>): ZonedDateTime {
        // Conduct a binary search to find the set time, with a max of 100 iterations
        var time = range.start
        var end = range.end
        var iterations = 0
        while (time < end && iterations < 100) {
            val mid = Range(time, end).middle()
            val altitude = getAltitude(parameters, mid)
            if (altitude < parameters.standardAltitude) {
                end = mid
            } else {
                time = mid
            }
            iterations++
        }

        return time
    }

    private fun getTransitTime(parameters: SearchParameters, range: Range<ZonedDateTime>): ZonedDateTime {
        var start = range.start

        var maxAltitude = -100f
        var maxAltitudeTime = start

        while (start < range.end) {
            val altitude = getAltitude(parameters, start)
            if (altitude < maxAltitude) {
                break
            }
            maxAltitude = altitude
            maxAltitudeTime = start
            start = start.plusMinutes(1)
        }
        return maxAltitudeTime
    }

    private fun getAltitude(searchParameters: SearchParameters, time: ZonedDateTime): Float {
        val ut = time.toUniversalTime()
        return AstroUtils.getAltitude(
            searchParameters.locator,
            ut,
            searchParameters.location,
            searchParameters.withRefraction,
            searchParameters.withParallax
        )
    }

    private data class SearchParameters(
        val locator: ICelestialLocator,
        val location: Coordinate,
        val standardAltitude: Double,
        val withRefraction: Boolean,
        val withParallax: Boolean
    )
}