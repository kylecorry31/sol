package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.andromeda.core.math.deltaAngle
import com.kylecorry.andromeda.core.math.sinDegrees
import com.kylecorry.andromeda.core.math.wrap
import com.kylecorry.andromeda.core.time.atStartOfDay
import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.CompassDirection
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.eclipse.Eclipse
import com.kylecorry.trailsensecore.domain.astronomy.eclipse.EclipseType
import com.kylecorry.trailsensecore.domain.astronomy.eclipse.PartialLunarEclipseCalculator
import com.kylecorry.trailsensecore.domain.astronomy.eclipse.TotalLunarEclipseCalculator
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonPhase
import com.kylecorry.trailsensecore.domain.time.DateUtils
import com.kylecorry.trailsensecore.domain.time.Season
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.math.absoluteValue

class AstronomyService : IAstronomyService {

    override fun getSunEvents(
        date: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode,
        withRefraction: Boolean
    ): RiseSetTransitTimes {

        val altitude = when (mode) {
            SunTimesMode.Actual -> -0.8333
            SunTimesMode.Civil -> -6.0
            SunTimesMode.Nautical -> -12.0
            SunTimesMode.Astronomical -> -18.0
        }

        return Astro.getSunTimes(date, location, altitude, withRefraction)
    }

    override fun getSunAltitude(
        time: ZonedDateTime, location: Coordinate,
        withRefraction: Boolean
    ): Float {
        val ut = Astro.ut(time)
        val jd = Astro.julianDay(ut)
        val solarCoordinates = Astro.solarCoordinates(jd)
        val hourAngle = Astro.hourAngle(
            Astro.meanSiderealTime(jd),
            location.longitude,
            solarCoordinates.rightAscension
        )
        return Astro.altitude(
            hourAngle,
            location.latitude,
            solarCoordinates.declination,
            withRefraction
        )
            .toFloat()
    }

    override fun getSunAzimuth(time: ZonedDateTime, location: Coordinate): Bearing {
        val ut = Astro.ut(time)
        val jd = Astro.julianDay(ut)
        val solarCoordinates = Astro.solarCoordinates(jd)
        val hourAngle = Astro.hourAngle(
            Astro.meanSiderealTime(jd),
            location.longitude,
            solarCoordinates.rightAscension
        )
        return Bearing(
            Astro.azimuth(hourAngle, location.latitude, solarCoordinates.declination).toFloat()
        )
    }

    override fun getNextSunset(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode,
        withRefraction: Boolean
    ): ZonedDateTime? {
        val today = getSunEvents(time, location, mode, withRefraction)
        val tomorrow = getSunEvents(time.plusDays(1), location, mode, withRefraction)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.set, tomorrow.set)
        )
    }

    override fun getNextSunrise(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode,
        withRefraction: Boolean
    ): ZonedDateTime? {
        val today = getSunEvents(time, location, mode, withRefraction)
        val tomorrow = getSunEvents(time.plusDays(1), location, mode, withRefraction)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.rise, tomorrow.rise)
        )
    }

    override fun isSunUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean
    ): Boolean {
        return getSunAltitude(time, location, withRefraction) > 0
    }

    override fun getDaylightLength(
        date: ZonedDateTime,
        location: Coordinate,
        sunTimesMode: SunTimesMode
    ): Duration {
        val startOfDay = date.atStartOfDay()
        val sunrise = getNextSunrise(startOfDay, location, sunTimesMode)
        val sunset = getNextSunset(startOfDay, location, sunTimesMode)

        if (sunrise != null && sunset != null && sunset > sunrise) {
            // Rise in morning, set at night
            return Duration.between(sunrise, sunset)
        } else if (sunrise == null && sunset == null) {
            // Sun doesn't rise or set
            return if (isSunUp(startOfDay, location)) Duration.between(
                startOfDay,
                startOfDay.plusDays(1)
            ) else Duration.ZERO
        } else if (sunrise != null && sunset == null) {
            // Sun rises but doesn't set
            return Duration.between(sunrise, startOfDay.plusDays(1))
        } else if (sunset != null && sunrise == null) {
            // Sun sets but doesn't rise
            return Duration.between(startOfDay, sunset)
        } else {
            // Sun sets in morning, rises at night
            return Duration.between(startOfDay, sunset)
                .plus(Duration.between(sunrise, startOfDay.plusDays(1)))
        }
    }


    private fun getOptimalSolarDirection(location: Coordinate): Bearing {
        return if (location.latitude > 0) {
            Bearing.from(CompassDirection.South)
        } else {
            Bearing.from(CompassDirection.North)
        }
    }

    override fun getBestSolarPanelPositionForRestOfDay(
        start: ZonedDateTime,
        location: Coordinate
    ): SolarPanelPosition {
        val interval = 5L

        var time = start

        var averageAzimuth = 0f
        var averageAltitude = 0f
        var count = 0

        while (time.toLocalDate() == start.toLocalDate()) {
            val altitude = getSunAltitude(time, location, true)

            val wrapAzimuth = !location.isNorthernHemisphere
            if (altitude >= 0) {
                var azimuth = getSunAzimuth(time, location).value
                if (wrapAzimuth && azimuth < 180f) {
                    azimuth += 360f
                }
                averageAzimuth += azimuth
                count++
            }
            time = time.plusMinutes(interval)
        }

        if (count != 0) {
            averageAzimuth /= count
        }

        // Only get altitudes close to the average azimuth
        time = start
        count = 0
        while (time.toLocalDate() == start.toLocalDate()) {
            val altitude = getSunAltitude(time, location, true)

            if (altitude >= 0) {
                val azimuth = getSunAzimuth(time, location).value
                if (deltaAngle(azimuth, averageAzimuth).absoluteValue < 45f) {
                    averageAltitude += altitude
                    count++
                }
            }
            time = time.plusMinutes(interval)
        }

        if (count != 0) {
            averageAltitude /= count
        }

        val angle = 90f - averageAltitude
        val direction = Bearing(averageAzimuth)
        return SolarPanelPosition(angle, direction)
    }


    override fun getBestSolarPanelPositionForDay(
        date: ZonedDateTime,
        location: Coordinate
    ): SolarPanelPosition {
        val start = date.withHour(0).withMinute(0).withSecond(0)
        return getBestSolarPanelPositionForRestOfDay(start, location)
    }

    override fun getBestSolarPanelPositionForTime(
        time: ZonedDateTime,
        location: Coordinate
    ): SolarPanelPosition {
        val sunAltitude = getSunAltitude(time, location, true)

        if (sunAltitude >= 0) {
            val azimuth = getSunAzimuth(time, location)
            return SolarPanelPosition(90f - sunAltitude, azimuth)
        }

        // Get azimuth of next sunrise
        val nextSunrise = getNextSunrise(time, location)
            ?: return getBestSolarPanelPositionForDay(time, location)

        val azimuth = getSunAzimuth(nextSunrise, location)
        return SolarPanelPosition(90f, azimuth)
    }

    override fun getMoonEvents(
        date: ZonedDateTime, location: Coordinate,
        withRefraction: Boolean
    ): RiseSetTransitTimes {
        return Astro.getMoonTimes(date, location, withRefraction = withRefraction)
    }

    override fun getMoonAltitude(
        time: ZonedDateTime, location: Coordinate,
        withRefraction: Boolean
    ): Float {
        val ut = Astro.ut(time)
        val jd = Astro.julianDay(ut)
        val lunarCoordinates = Astro.lunarCoordinates(jd)
        val hourAngle = Astro.hourAngle(
            Astro.meanSiderealTime(jd),
            location.longitude,
            lunarCoordinates.rightAscension
        )
        return Astro.altitude(
            hourAngle,
            location.latitude,
            lunarCoordinates.declination,
            withRefraction
        )
            .toFloat()
    }

    override fun getMoonAzimuth(time: ZonedDateTime, location: Coordinate): Bearing {
        val ut = Astro.ut(time)
        val jd = Astro.julianDay(ut)
        val lunarCoordinates = Astro.lunarCoordinates(jd)
        val hourAngle = Astro.hourAngle(
            Astro.meanSiderealTime(jd),
            location.longitude,
            lunarCoordinates.rightAscension
        )
        return Bearing(
            Astro.azimuth(hourAngle, location.latitude, lunarCoordinates.declination).toFloat()
        )
    }

    override fun getNextMoonset(
        time: ZonedDateTime, location: Coordinate,
        withRefraction: Boolean
    ): ZonedDateTime? {
        val today = getMoonEvents(time, location, withRefraction)
        val tomorrow = getMoonEvents(time.plusDays(1), location, withRefraction)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.set, tomorrow.set)
        )
    }

    override fun getNextMoonrise(
        time: ZonedDateTime, location: Coordinate,
        withRefraction: Boolean
    ): ZonedDateTime? {
        val today = getMoonEvents(time, location, withRefraction)
        val tomorrow = getMoonEvents(time.plusDays(1), location, withRefraction)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.rise, tomorrow.rise)
        )
    }

    override fun getMoonPhase(date: ZonedDateTime): MoonPhase {
        return Astro.getMoonPhase(date)
    }

    override fun isMoonUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean
    ): Boolean {
        return getMoonAltitude(time, location, withRefraction) > 0
    }

    override fun getAstronomicalSeason(location: Coordinate, date: ZonedDateTime): Season {
        val sl = wrap(getSolarLongitude(date), 0f, 360f)
        return when {
            sl >= OrbitalPosition.WinterSolstice.solarLongitude -> if (location.isNorthernHemisphere) Season.Winter else Season.Summer
            sl >= OrbitalPosition.AutumnalEquinox.solarLongitude -> if (location.isNorthernHemisphere) Season.Fall else Season.Spring
            sl >= OrbitalPosition.SummerSolstice.solarLongitude -> if (location.isNorthernHemisphere) Season.Summer else Season.Winter
            else -> if (location.isNorthernHemisphere) Season.Spring else Season.Fall
        }
    }

    override fun getNextEclipse(
        time: Instant,
        location: Coordinate,
        type: EclipseType
    ): Eclipse? {
        val calculator = when (type) {
            EclipseType.PartialLunar -> PartialLunarEclipseCalculator()
            EclipseType.TotalLunar -> TotalLunarEclipseCalculator()
        }
        return calculator.getNextEclipse(time, location)
    }

    override fun getMeteorShower(location: Coordinate, date: ZonedDateTime): MeteorShowerPeak? {
        val startOfDay = ZonedDateTime.of(date.toLocalDate(), LocalTime.MIN, date.zone)

        val solarLongitude = getSolarLongitude(date)

        for (shower in MeteorShower.values()) {
            if (deltaAngle(solarLongitude, shower.solarLongitude).absoluteValue > 1) {
                continue
            }

            val peak = getNextMeteorShowerPeak(shower, location, startOfDay)

            if (peak?.toLocalDate() == date.toLocalDate()) {
                return MeteorShowerPeak(shower, peak!!)
            }
        }

        return null
    }

    private fun getNextMeteorShowerPeak(
        shower: MeteorShower,
        location: Coordinate,
        now: ZonedDateTime
    ): ZonedDateTime? {
        val time = getNextTimeAtSolarLongitude(shower.solarLongitude, now)
        val today = getMeteorShowerTimes(shower, location, time)
        val yesterday = getMeteorShowerTimes(shower, location, time.plusDays(1))
        val tomorrow = getMeteorShowerTimes(shower, location, time.minusDays(1))

        val closest = DateUtils.getClosestTime(
            time,
            listOf(today.transit, yesterday.transit, tomorrow.transit)
        )

        if (closest == null && isMeteorShowerVisible(shower, location, time)) {
            // Doesn't set
            val sun = getSunEvents(time, location, SunTimesMode.Astronomical)
            return if (!isSunUp(time, location, false)) {
                time
            } else if (sun.rise != null && isMeteorShowerVisible(
                    shower,
                    location,
                    sun.rise.minusHours(1)
                )
            ) {
                sun.rise.minusHours(1)
            } else {
                null
            }
        } else if (closest != null) {
            // Sets, use the transit point
            val sun = getSunEvents(closest, location, SunTimesMode.Astronomical)
            return if (!isSunUp(closest, location, false)) {
                closest
            } else if (sun.rise != null && isMeteorShowerVisible(
                    shower,
                    location,
                    sun.rise.minusHours(1)
                )
            ) {
                sun.rise.minusHours(1)
            } else {
                null
            }
        }

        return null
    }

    private fun isMeteorShowerVisible(
        shower: MeteorShower,
        location: Coordinate,
        time: ZonedDateTime
    ): Boolean {
        val showerAltitude = getMeteorShowerAltitude(shower, location, time)
        return showerAltitude > 0
    }

    private fun getMeteorShowerTimes(
        shower: MeteorShower,
        location: Coordinate,
        date: ZonedDateTime
    ): RiseSetTransitTimes {
        return Astro.getTransitEvents(
            date,
            location,
            0.0,
            false
        ) {
            shower.radiant
        }
    }

    private fun getMeteorShowerAltitude(
        shower: MeteorShower,
        location: Coordinate,
        time: ZonedDateTime
    ): Float {
        val ut = Astro.ut(time)
        val jd = Astro.julianDay(ut)
        val hourAngle = Astro.hourAngle(
            Astro.meanSiderealTime(jd),
            location.longitude,
            shower.radiant.rightAscension
        )
        return Astro.altitude(
            hourAngle,
            location.latitude,
            shower.radiant.declination,
            false
        )
            .toFloat()
    }

    private fun getNextTimeAtSolarLongitude(longitude: Float, today: ZonedDateTime): ZonedDateTime {
        val threshold = 1f
        var d = today
        for (i in 0..365) {
            val date = today.plusDays(i.toLong())
            val sl = getSolarLongitude(date)
            if (deltaAngle(longitude, sl).absoluteValue < threshold) {
                d = date
                break
            }
        }

        var jd = Astro.julianDay(Astro.ut(d))
        var correction: Double

        do {
            correction = 58 * sinDegrees(longitude - Astro.sunTrueLongitude(jd))
            jd += correction
        } while (correction > 0.00001)

        return Astro.utToLocal(Astro.utFromJulianDay(jd), today.zone)
    }

    private fun getSolarLongitude(date: ZonedDateTime): Float {
        val jd = Astro.julianDay(Astro.ut(date))
        return Astro.sunTrueLongitude(jd).toFloat()
    }

}