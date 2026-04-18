package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.trigonometry.Trigonometry.deltaAngle
import com.kylecorry.sol.science.astronomy.locators.MeteorShowerLocator
import com.kylecorry.sol.science.astronomy.meteors.MeteorShower
import com.kylecorry.sol.science.astronomy.meteors.MeteorShowerPeak
import com.kylecorry.sol.science.astronomy.rst.NewtonsRiseSetTransitTimeCalculator
import com.kylecorry.sol.science.astronomy.units.CelestialObservation
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.time.Time.getClosestFutureTime
import com.kylecorry.sol.time.Time.getClosestPastTime
import com.kylecorry.sol.time.Time.getClosestTime
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.math.absoluteValue

internal object MeteorFacade {
    fun getMeteorShower(location: Coordinate, date: ZonedDateTime): MeteorShowerPeak? {
        val startOfDay = ZonedDateTime.of(date.toLocalDate(), LocalTime.MIN, date.zone)

        val solarLongitude = SunFacade.getSolarLongitude(date)

        for (shower in MeteorShower.entries) {
            if (deltaAngle(solarLongitude, shower.solarLongitude).absoluteValue > 2) {
                continue
            }

            val peak = getNextMeteorShowerPeak(shower, location, startOfDay) ?: continue
            peak.transit ?: continue

            if (peak.transit.toLocalDate() == date.toLocalDate()) {
                return MeteorShowerPeak(
                    shower,
                    peak.rise ?: peak.transit,
                    peak.transit,
                    peak.set ?: peak.transit
                )
            }
        }

        return null
    }

    /**
     * Get a list of meteor showers which are active.
     * This does not check the time of day, so the shower may not currently be visible.
     */
    fun getActiveMeteorShowers(
        location: Coordinate,
        date: ZonedDateTime
    ): List<MeteorShowerPeak> {
        val active = mutableSetOf<MeteorShowerPeak>()
        val searchRange = MeteorShower.entries.maxOf { it.activeDays }

        val start = date.minusDays(searchRange.toLong())
        val end = date.plusDays(searchRange.toLong())
        var current = start
        while (current.isBefore(end)) {
            val peak = getMeteorShower(location, current)
            if (peak != null && Duration.between(peak.peak, date)
                    .abs() <= Duration.ofDays(peak.shower.activeDays.toLong() / 2)
            ) {
                active.add(peak)
            }
            current = current.plusDays(1)
        }

        return active.toList()
    }

    fun getMeteorShowerPosition(
        shower: MeteorShower,
        location: Coordinate,
        time: Instant
    ): CelestialObservation {
        val ut = time.toUniversalTime()
        val locator = MeteorShowerLocator(shower)
        val horizonCoordinate = AstroUtils.getLocation(
            locator,
            ut,
            location
        )
        return CelestialObservation(
            Bearing.from(horizonCoordinate.azimuth.toFloat()),
            horizonCoordinate.altitude.toFloat()
        )
    }

    private fun getNextMeteorShowerPeak(
        shower: MeteorShower,
        location: Coordinate,
        now: ZonedDateTime
    ): RiseSetTransitTimes? {
        val time = SunFacade.getNextTimeAtSolarLongitude(shower.solarLongitude, now)
        val today = getMeteorShowerTimes(shower, location, time)
        val yesterday = getMeteorShowerTimes(shower, location, time.minusDays(1))
        val tomorrow = getMeteorShowerTimes(shower, location, time.plusDays(1))

        val transit = getClosestTime(
            time,
            listOf(yesterday.transit, today.transit, tomorrow.transit)
        )

        val rise = getClosestPastTime(
            transit ?: time,
            listOf(yesterday.rise, today.rise, tomorrow.rise)
        )

        val set = getClosestFutureTime(
            transit ?: time,
            listOf(yesterday.set, today.set, tomorrow.set)
        )

        val night = SunFacade.getClosestNight(
            transit ?: time,
            location,
            SunTimesMode.Astronomical
        ) ?: return null

        if (transit == null) {
            // Check to see when it is visible
            var currentTime = night.start
            var peakAltitude = -1f
            var peakTime = currentTime
            while (currentTime.isBefore(night.end)) {
                val altitude = getMeteorShowerPosition(shower, location, currentTime.toInstant()).altitude
                if (altitude > peakAltitude) {
                    peakAltitude = altitude
                    peakTime = currentTime
                }
                currentTime = currentTime.plusMinutes(5)
            }

            if (peakAltitude < 0) {
                return null
            }

            return RiseSetTransitTimes(night.start, peakTime, night.end)
        }

        // Shower rises and sets

        val times = Range(rise ?: night.start, set ?: night.end)

        // Restrict to night
        val intersection = times.intersection(night) ?: return null

        val realTransit = intersection.clamp(transit)

        return RiseSetTransitTimes(intersection.start, realTransit, intersection.end)
    }

    private fun getMeteorShowerTimes(
        shower: MeteorShower,
        location: Coordinate,
        date: ZonedDateTime
    ): RiseSetTransitTimes {
        // Purposefully use newton's method here to get the rise and set times,
        // since missing rise/set/transit times are expected
        return NewtonsRiseSetTransitTimeCalculator().calculate(
            MeteorShowerLocator(shower),
            date,
            location,
            0.0,
            false
        )
    }
}
