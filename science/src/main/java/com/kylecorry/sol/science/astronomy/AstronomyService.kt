package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.math.SolMath.deltaAngle
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.wrap
import com.kylecorry.sol.science.astronomy.eclipse.Eclipse
import com.kylecorry.sol.science.astronomy.eclipse.EclipseType
import com.kylecorry.sol.science.astronomy.eclipse.PartialLunarEclipseCalculator
import com.kylecorry.sol.science.astronomy.eclipse.TotalLunarEclipseCalculator
import com.kylecorry.sol.science.astronomy.locators.MeteorShowerLocator
import com.kylecorry.sol.science.astronomy.locators.Moon
import com.kylecorry.sol.science.astronomy.locators.Sun
import com.kylecorry.sol.science.astronomy.meteors.MeteorShower
import com.kylecorry.sol.science.astronomy.meteors.MeteorShowerPeak
import com.kylecorry.sol.science.astronomy.moon.MoonPhase
import com.kylecorry.sol.science.astronomy.moon.MoonTruePhase
import com.kylecorry.sol.science.astronomy.sun.SolarRadiationCalculator
import com.kylecorry.sol.science.astronomy.units.*
import com.kylecorry.sol.science.astronomy.units.EclipticCoordinate
import com.kylecorry.sol.science.shared.Season
import com.kylecorry.sol.time.Time.atStartOfDay
import com.kylecorry.sol.time.Time.getClosestFutureTime
import com.kylecorry.sol.time.Time.getClosestTime
import com.kylecorry.sol.units.*
import java.time.*
import kotlin.math.absoluteValue

class AstronomyService : IAstronomyService {

    private val sun = Sun()
    private val moon = Moon()
    private val radiation = SolarRadiationCalculator()

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

        return RiseSetTransitTimeCalculator().calculate(
            sun,
            date,
            location,
            altitude,
            withRefraction
        )
    }

    override fun getSunAltitude(
        time: ZonedDateTime, location: Coordinate,
        withRefraction: Boolean
    ): Float {
        return AstroUtils.getAltitude(sun, time.toUniversalTime(), location, withRefraction)
    }

    override fun getSunAzimuth(time: ZonedDateTime, location: Coordinate): Bearing {
        return AstroUtils.getAzimuth(sun, time.toUniversalTime(), location)
    }

    override fun getNextSunset(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode,
        withRefraction: Boolean
    ): ZonedDateTime? {
        val today = getSunEvents(time, location, mode, withRefraction)
        val tomorrow = getSunEvents(time.plusDays(1), location, mode, withRefraction)
        return getClosestFutureTime(
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
        return getClosestFutureTime(
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

    override fun getSunDistance(time: ZonedDateTime): Distance {
        return sun.getDistance(time.toUniversalTime())
    }

    override fun getSolarRadiation(date: ZonedDateTime, location: Coordinate): Double {
        return radiation.getRadiation(date.toUniversalTime(), location)
    }

    override fun getSolarRadiation(
        date: ZonedDateTime,
        location: Coordinate,
        tilt: Float,
        azimuth: Bearing
    ): Double {
        return radiation.getRadiation(date.toUniversalTime(), location, tilt, azimuth)
    }

    override fun getMoonEvents(
        date: ZonedDateTime, location: Coordinate,
        withRefraction: Boolean
    ): RiseSetTransitTimes {
        return RiseSetTransitTimeCalculator().calculate(
            moon,
            date,
            location,
            0.125,
            withRefraction
        )
    }

    override fun getMoonAltitude(
        time: ZonedDateTime, location: Coordinate,
        withRefraction: Boolean
    ): Float {
        return AstroUtils.getAltitude(moon, time.toUniversalTime(), location, withRefraction)
    }

    override fun getMoonAzimuth(time: ZonedDateTime, location: Coordinate): Bearing {
        return AstroUtils.getAzimuth(moon, time.toUniversalTime(), location)
    }

    override fun getNextMoonset(
        time: ZonedDateTime, location: Coordinate,
        withRefraction: Boolean
    ): ZonedDateTime? {
        val today = getMoonEvents(time, location, withRefraction)
        val tomorrow = getMoonEvents(time.plusDays(1), location, withRefraction)
        return getClosestFutureTime(
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
        return getClosestFutureTime(
            time,
            listOf(today.rise, tomorrow.rise)
        )
    }

    override fun getMoonPhase(date: ZonedDateTime): MoonPhase {
        return moon.getPhase(date.toUniversalTime())
    }

    override fun isMoonUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean
    ): Boolean {
        return getMoonAltitude(time, location, withRefraction) > 0
    }

    override fun getMoonDistance(time: ZonedDateTime): Distance {
        return moon.getDistance(time.toUniversalTime())
    }

    override fun isSuperMoon(time: ZonedDateTime): Boolean {
        val phase = getMoonPhase(time)
        if (phase.phase != MoonTruePhase.Full) {
            return false
        }
        val distance = getMoonDistance(time)
        return distance.convertTo(DistanceUnits.Kilometers).distance <= 360000f
    }

    override fun getSeason(location: Coordinate, date: ZonedDateTime): Season {
        val sl = wrap(getSolarLongitude(date), 0f, 360f)
        return when {
            sl >= OrbitalPosition.WinterSolstice.solarLongitude -> if (location.isNorthernHemisphere) Season.Winter else Season.Summer
            sl >= OrbitalPosition.AutumnalEquinox.solarLongitude -> if (location.isNorthernHemisphere) Season.Fall else Season.Spring
            sl >= OrbitalPosition.SummerSolstice.solarLongitude -> if (location.isNorthernHemisphere) Season.Summer else Season.Winter
            else -> if (location.isNorthernHemisphere) Season.Spring else Season.Fall
        }
    }

    override fun getNextEclipse(
        time: ZonedDateTime,
        location: Coordinate,
        type: EclipseType
    ): Eclipse? {
        val calculator = when (type) {
            EclipseType.PartialLunar -> PartialLunarEclipseCalculator()
            EclipseType.TotalLunar -> TotalLunarEclipseCalculator()
        }
        return calculator.getNextEclipse(time.toInstant(), location)
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

    override fun getMeteorShowerAltitude(
        shower: MeteorShower,
        location: Coordinate,
        time: Instant
    ): Float {
        val locator = MeteorShowerLocator(shower)
        return AstroUtils.getAltitude(locator, time.toUniversalTime(), location, false)
    }

    override fun getMeteorShowerAzimuth(
        shower: MeteorShower,
        location: Coordinate,
        time: Instant
    ): Bearing {
        val locator = MeteorShowerLocator(shower)
        return AstroUtils.getAzimuth(locator, time.toUniversalTime(), location)
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

        val closest = getClosestTime(
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
        val showerAltitude = getMeteorShowerAltitude(shower, location, time.toInstant())
        return showerAltitude > 0
    }

    private fun getMeteorShowerTimes(
        shower: MeteorShower,
        location: Coordinate,
        date: ZonedDateTime
    ): RiseSetTransitTimes {
        return RiseSetTransitTimeCalculator().calculate(
            MeteorShowerLocator(shower),
            date,
            location,
            0.0,
            false
        )
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

        var jd = d.toUniversalTime().toJulianDay()
        var correction: Double

        do {
            val ut = fromJulianDay(jd)
            val coords = sun.getCoordinates(ut)
            val solarLon = EclipticCoordinate.fromEquatorial(
                coords,
                ut
            ).eclipticLongitude
            correction = 58 * sinDegrees(longitude - solarLon)
            jd += correction
        } while (correction > 0.00001)

        return fromJulianDay(jd).toLocal(today.zone)
    }

    private fun getSolarLongitude(date: ZonedDateTime): Float {
        val coords = sun.getCoordinates(date.toUniversalTime())
        return EclipticCoordinate.fromEquatorial(
            coords,
            date.toUniversalTime()
        ).eclipticLongitude.toFloat()
    }

}