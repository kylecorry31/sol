package com.kylecorry.sol.science.astronomy.rst

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.astronomy.AstroSearch
import com.kylecorry.sol.science.astronomy.AstroUtils
import com.kylecorry.sol.science.astronomy.RiseSetTransitTimes
import com.kylecorry.sol.science.astronomy.locators.ICelestialLocator
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.time.Time.atStartOfDay
import com.kylecorry.sol.units.Coordinate
import java.time.*

internal class SearchRiseSetTransitTimeCalculator : IRiseSetTransitTimeCalculator {
    override fun calculate(
        locator: ICelestialLocator,
        date: ZonedDateTime,
        location: Coordinate,
        standardAltitude: Double,
        withRefraction: Boolean,
        withParallax: Boolean
    ): RiseSetTransitTimes {
        val parameters = SearchParameters(
            locator,
            location,
            standardAltitude,
            withRefraction,
            withParallax
        )
        val eventTimeRanges = findHourlyEventRanges(parameters, date)

        // Narrow down the actual times
        val rise = eventTimeRanges.rise?.let {
            AstroSearch.findStart(getSearchRange(it), SEARCH_PRECISION) { time ->
                getAltitude(parameters, time.toEpochMilli()) >= standardAltitude
            }?.atZone(date.zone)
        }

        val set = eventTimeRanges.set?.let {
            AstroSearch.findEnd(getSearchRange(it), SEARCH_PRECISION) { time ->
                getAltitude(parameters, time.toEpochMilli()) >= standardAltitude
            }?.atZone(date.zone)
        }

        val transit = eventTimeRanges.transit?.let {
            val peak = AstroSearch.findPeak(getSearchRange(it), SEARCH_PRECISION) { time ->
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

    private fun getSearchRange(times: Range<ZonedDateTime>): Range<Instant> {
        return Range(times.start.toInstant() - SEARCH_PRECISION, times.end.toInstant() + SEARCH_PRECISION)
    }

    private fun findHourlyEventRanges(
        parameters: SearchParameters,
        date: ZonedDateTime
    ): EventTimeRanges {
        var time = date.atStartOfDay()
        val currentDate = date.toLocalDate()
        val yesterdayAltitude = getAltitude(parameters, time.minusHours(1))
        var lastAltitude = getAltitude(parameters, time)
        var isRising = lastAltitude > yesterdayAltitude

        var setTimeRange: Range<ZonedDateTime>? = null
        var riseTimeRange: Range<ZonedDateTime>? = null
        var transitTimeRange: Range<ZonedDateTime>? = null

        while (time.toLocalDate() == currentDate) {
            val altitude = getAltitude(parameters, time)

            if (lastAltitude >= parameters.standardAltitude && altitude < parameters.standardAltitude) {
                setTimeRange = Range(time.minusHours(SEARCH_INTERVAL_HOURS), time)
            }

            if (parameters.standardAltitude in lastAltitude.toDouble()..<altitude.toDouble()) {
                riseTimeRange = Range(time.minusHours(SEARCH_INTERVAL_HOURS), time)
            }

            if (transitTimeRange == null) {
                val isCurrentlyRising = altitude > getAltitude(parameters, time.minusMinutes(1))
                if (isRising && !isCurrentlyRising && altitude >= parameters.standardAltitude) {
                    transitTimeRange = Range(time.minusHours(SEARCH_INTERVAL_HOURS), time)
                }
                isRising = isCurrentlyRising
            }

            if (setTimeRange != null && riseTimeRange != null && transitTimeRange != null) {
                break
            }

            lastAltitude = altitude
            time = time.plusHours(SEARCH_INTERVAL_HOURS)
        }

        return EventTimeRanges(riseTimeRange, transitTimeRange, setTimeRange)
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

    private data class EventTimeRanges(
        val rise: Range<ZonedDateTime>?,
        val transit: Range<ZonedDateTime>?,
        val set: Range<ZonedDateTime>?
    )

    companion object {
        private const val SEARCH_INTERVAL_HOURS = 1L
        private val SEARCH_PRECISION = Duration.ofMinutes(1)
    }
}
