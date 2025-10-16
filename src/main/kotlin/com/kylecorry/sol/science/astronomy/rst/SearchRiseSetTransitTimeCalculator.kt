package com.kylecorry.sol.science.astronomy.rst
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.astronomy.AstroSearch
import com.kylecorry.sol.science.astronomy.AstroUtils
import com.kylecorry.sol.science.astronomy.RiseSetTransitTimes
import com.kylecorry.sol.science.astronomy.locators.ICelestialLocator
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.time.Time.atStartOfDay
import com.kylecorry.sol.time.Time.middle
import com.kylecorry.sol.time.Time.plusMillis
import com.kylecorry.sol.time.Time.toUTCLocal
import com.kylecorry.sol.time.Time.toZonedDateTime
import com.kylecorry.sol.units.Coordinate

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

        val intervalHours = 1L

        // Find the hour of each event
        while (time.toLocalDate() == currentDate) {
            val altitude = getAltitude(parameters, time)

            // Check if it just set past the standard altitude
            if (lastAltitude >= standardAltitude && altitude < standardAltitude) {
                setTimeRange = Range(time.minusHours(intervalHours), time)
            }

            // Check if it just rose past the standard altitude
            if (lastAltitude <= standardAltitude && altitude > standardAltitude) {
                riseTimeRange = Range(time.minusHours(intervalHours), time)
            }

            // Check if it just crossed transit
            if (transitTimeRange == null) {
                val isCurrentlyRising = altitude > getAltitude(parameters, time.minusMinutes(1))
                if (isRising && !isCurrentlyRising && altitude >= standardAltitude) {
                    transitTimeRange = Range(time.minusHours(intervalHours), time)
                }
                isRising = isCurrentlyRising
            }

            // If all three times are found, break
            if (setTimeRange != null && riseTimeRange != null && transitTimeRange != null) {
                break
            }

            lastAltitude = altitude
            time = time.plusHours(intervalHours)
        }

        // Narrow down the times to within 1 minute
        val precision = (1).minutes

        val rise = riseTimeRange?.let {
            AstroSearch.findStart(
                Range(it.start.toInstant() - precision, it.end.toInstant() + precision),
                precision
            ) { time ->
                getAltitude(parameters, time.toEpochMilli()) >= standardAltitude
            }?.atZone(date.zone)
        }

        val set = setTimeRange?.let {
            AstroSearch.findEnd(
                Range(it.start.toInstant() - precision, it.end.toInstant() + precision),
                precision
            ) { time ->
                getAltitude(parameters, time.toEpochMilli()) >= standardAltitude
            }?.atZone(date.zone)
        }

        val transit = transitTimeRange?.let {
            val peak = AstroSearch.findPeak(
                Range(it.start.toInstant() - precision, it.end.toInstant() + precision),
                precision
            ) { time ->
                getAltitude(parameters, time.toEpochMilli())
            }.atZone(date.zone)

            if (getAltitude(parameters, peak) >= standardAltitude) {
                peak
            } else {
                null
            }
        }

        return RiseSetTransitTimes(rise, transit, set)
    }

    private fun getAltitude(searchParameters: SearchParameters, time: Long): Float {
        val ut = LocalDateTime.ofEpochSecond(time / 1000, 0, ZoneOffset.UTC)
        return AstroUtils.getAltitude(
            searchParameters.locator,
            ut,
            searchParameters.location,
            searchParameters.withRefraction,
            searchParameters.withParallax
        )
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