package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.SolMath.deltaAngle
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.wrap
import com.kylecorry.sol.science.astronomy.eclipse.Eclipse
import com.kylecorry.sol.science.astronomy.eclipse.EclipseType
import com.kylecorry.sol.science.astronomy.eclipse.lunar.PartialLunarEclipseCalculator
import com.kylecorry.sol.science.astronomy.eclipse.lunar.TotalLunarEclipseCalculator
import com.kylecorry.sol.science.astronomy.eclipse.solar.SolarEclipseCalculator
import com.kylecorry.sol.science.astronomy.locators.MeteorShowerLocator
import com.kylecorry.sol.science.astronomy.locators.Moon
import com.kylecorry.sol.science.astronomy.locators.StarLocator
import com.kylecorry.sol.science.astronomy.locators.Sun
import com.kylecorry.sol.science.astronomy.meteors.MeteorShower
import com.kylecorry.sol.science.astronomy.meteors.MeteorShowerPeak
import com.kylecorry.sol.science.astronomy.moon.MoonPhase
import com.kylecorry.sol.science.astronomy.moon.MoonTruePhase
import com.kylecorry.sol.science.astronomy.rst.NewtonsRiseSetTransitTimeCalculator
import com.kylecorry.sol.science.astronomy.rst.RobustRiseSetTransitTimeCalculator
import com.kylecorry.sol.science.astronomy.stars.AltitudeAzimuth
import com.kylecorry.sol.science.astronomy.stars.PlateSolver
import com.kylecorry.sol.science.astronomy.stars.Star
import com.kylecorry.sol.science.astronomy.stars.StarLocationCalculator
import com.kylecorry.sol.science.astronomy.stars.StarReading
import com.kylecorry.sol.science.astronomy.sun.SolarRadiationCalculator
import com.kylecorry.sol.science.astronomy.units.*
import com.kylecorry.sol.science.shared.Season
import com.kylecorry.sol.time.Time
import com.kylecorry.sol.time.Time.atEndOfDay
import com.kylecorry.sol.time.Time.atStartOfDay
import com.kylecorry.sol.time.Time.getClosestFutureTime
import com.kylecorry.sol.time.Time.getClosestPastTime
import com.kylecorry.sol.time.Time.getClosestTime
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.DistanceUnits
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.math.absoluteValue

object Astronomy : IAstronomyService {

    private val sun = Sun()
    private val moon = Moon()
    private val radiation = SolarRadiationCalculator()
    private val riseSetTransitCalculator = RobustRiseSetTransitTimeCalculator()

    override fun getSunEvents(
        date: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode,
        withRefraction: Boolean,
        withParallax: Boolean
    ): RiseSetTransitTimes {

        val altitude = when (mode) {
            SunTimesMode.Actual -> -0.8333
            SunTimesMode.Civil -> -6.0
            SunTimesMode.Nautical -> -12.0
            SunTimesMode.Astronomical -> -18.0
        }

        return riseSetTransitCalculator.calculate(
            sun,
            date,
            location,
            altitude,
            withRefraction,
            withParallax
        )
    }

    override fun getSunAltitude(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean,
        withParallax: Boolean
    ): Float {
        return AstroUtils.getAltitude(
            sun,
            time.toUniversalTime(),
            location,
            withRefraction,
            withParallax
        )
    }

    override fun getSunAzimuth(
        time: ZonedDateTime,
        location: Coordinate,
        withParallax: Boolean
    ): Bearing {
        return AstroUtils.getAzimuth(sun, time.toUniversalTime(), location, withParallax)
    }

    override fun getNextSunset(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode,
        withRefraction: Boolean,
        withParallax: Boolean
    ): ZonedDateTime? {
        val today = getSunEvents(time, location, mode, withRefraction, withParallax)
        if (today.set != null && today.set > time) {
            return today.set
        }

        val tomorrow = getSunEvents(time.plusDays(1), location, mode, withRefraction, withParallax)
        if (tomorrow.set != null && tomorrow.set > time) {
            return tomorrow.set
        }

        return null
    }

    override fun getNextSunrise(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode,
        withRefraction: Boolean,
        withParallax: Boolean
    ): ZonedDateTime? {
        val today = getSunEvents(time, location, mode, withRefraction, withParallax)
        if (today.rise != null && today.rise > time) {
            return today.rise
        }

        val tomorrow = getSunEvents(time.plusDays(1), location, mode, withRefraction, withParallax)
        if (tomorrow.rise != null && tomorrow.rise > time) {
            return tomorrow.rise
        }

        return null
    }

    override fun isSunUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean,
        withParallax: Boolean
    ): Boolean {
        return getSunAltitude(time, location, withRefraction, withParallax) > 0
    }

    override fun getDaylightLength(
        date: ZonedDateTime,
        location: Coordinate,
        sunTimesMode: SunTimesMode,
        withRefraction: Boolean,
        withParallax: Boolean
    ): Duration {
        val startOfDay = date.atStartOfDay()
        val sunrise =
            getNextSunrise(startOfDay, location, sunTimesMode, withRefraction, withParallax)
        val sunset = getNextSunset(startOfDay, location, sunTimesMode, withRefraction, withParallax)

        if (sunrise != null && sunset != null && sunset > sunrise) {
            // Rise in morning, set at night
            return Duration.between(sunrise, sunset)
        } else if (sunrise == null && sunset == null) {
            // Sun doesn't rise or set
            return if (isSunUp(
                    startOfDay,
                    location,
                    withRefraction,
                    withParallax
                )
            ) Duration.between(
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

    override fun getSolarRadiation(
        date: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean,
        withParallax: Boolean
    ): Double {
        return radiation.getRadiation(
            date.toUniversalTime(),
            location,
            withRefraction = withRefraction,
            withParallax = withParallax
        )
    }

    override fun getSolarRadiation(
        date: ZonedDateTime,
        location: Coordinate,
        tilt: Float,
        azimuth: Bearing,
        withRefraction: Boolean,
        withParallax: Boolean
    ): Double {
        return radiation.getRadiation(
            date.toUniversalTime(),
            location,
            tilt,
            azimuth,
            withRefraction,
            withParallax
        )
    }

    override fun getSunAboveHorizonTimes(
        location: Coordinate,
        time: ZonedDateTime,
        nextRiseOffset: Duration,
        mode: SunTimesMode,
        withRefraction: Boolean,
        withParallax: Boolean
    ): Range<ZonedDateTime>? {
        return getAboveHorizonTimes(
            location,
            time,
            nextRiseOffset,
            { loc, t -> isSunUp(t, loc, withRefraction, withParallax) },
            { loc, t -> getSunEvents(t, loc, mode, withRefraction, withParallax) }
        )
    }

    override fun getMoonEvents(
        date: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean,
        withParallax: Boolean
    ): RiseSetTransitTimes {
        return riseSetTransitCalculator.calculate(
            moon,
            date,
            location,
            0.125,
            withRefraction,
            withParallax
        )
    }

    override fun getMoonAltitude(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean,
        withParallax: Boolean
    ): Float {
        return AstroUtils.getAltitude(
            moon,
            time.toUniversalTime(),
            location,
            withRefraction,
            withParallax
        )
    }

    override fun getMoonAzimuth(
        time: ZonedDateTime,
        location: Coordinate,
        withParallax: Boolean
    ): Bearing {
        return AstroUtils.getAzimuth(moon, time.toUniversalTime(), location, withParallax)
    }

    override fun getNextMoonset(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean,
        withParallax: Boolean
    ): ZonedDateTime? {
        val today = getMoonEvents(time, location, withRefraction, withParallax)
        if (today.set != null && today.set > time) {
            return today.set
        }

        val tomorrow = getMoonEvents(time.plusDays(1), location, withRefraction, withParallax)
        if (tomorrow.set != null && tomorrow.set > time) {
            return tomorrow.set
        }

        return null
    }

    override fun getNextMoonrise(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean,
        withParallax: Boolean
    ): ZonedDateTime? {
        val today = getMoonEvents(time, location, withRefraction, withParallax)
        if (today.rise != null && today.rise > time) {
            return today.rise
        }

        val tomorrow = getMoonEvents(time.plusDays(1), location, withRefraction, withParallax)
        if (tomorrow.rise != null && tomorrow.rise > time) {
            return tomorrow.rise
        }

        return null
    }

    override fun getMoonPhase(date: ZonedDateTime): MoonPhase {
        return moon.getPhase(date.toUniversalTime())
    }

    override fun isMoonUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean,
        withParallax: Boolean
    ): Boolean {
        return getMoonAltitude(time, location, withRefraction, withParallax) > 0
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

    override fun getMoonAboveHorizonTimes(
        location: Coordinate,
        time: ZonedDateTime,
        nextRiseOffset: Duration,
        withRefraction: Boolean,
        withParallax: Boolean
    ): Range<ZonedDateTime>? {
        return getAboveHorizonTimes(
            location,
            time,
            Duration.ofHours(6),
            { loc, t -> isMoonUp(t, loc, withRefraction, withParallax) },
            { loc, t -> getMoonEvents(t, loc, withRefraction, withParallax) }
        )
    }

    override fun getMoonTilt(time: ZonedDateTime, location: Coordinate): Float {
        return moon.getTilt(time.toUniversalTime(), location)
    }

    override fun getMoonParallacticAngle(time: ZonedDateTime, location: Coordinate): Float {
        return AstroUtils.getParallacticAngle(moon, time.toUniversalTime(), location)
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
        type: EclipseType,
        maxSearch: Duration?
    ): Eclipse? {
        // TODO: Apply max search to lunar as well
        val calculator = when (type) {
            EclipseType.PartialLunar -> PartialLunarEclipseCalculator()
            EclipseType.TotalLunar -> TotalLunarEclipseCalculator()
            EclipseType.Solar -> SolarEclipseCalculator(maxDuration = maxSearch)
        }
        return calculator.getNextEclipse(time.toInstant(), location)
    }

    override fun getEclipseMagnitude(time: ZonedDateTime, location: Coordinate, type: EclipseType): Float? {
        val calculator = when (type) {
            EclipseType.PartialLunar -> PartialLunarEclipseCalculator()
            EclipseType.TotalLunar -> TotalLunarEclipseCalculator()
            EclipseType.Solar -> SolarEclipseCalculator()
        }

        return calculator.getMagnitude(time.toInstant(), location)
    }

    override fun getEclipseObscuration(time: ZonedDateTime, location: Coordinate, type: EclipseType): Float? {
        val calculator = when (type) {
            EclipseType.PartialLunar -> PartialLunarEclipseCalculator()
            EclipseType.TotalLunar -> TotalLunarEclipseCalculator()
            EclipseType.Solar -> SolarEclipseCalculator()
        }

        return calculator.getObscuration(time.toInstant(), location)
    }

    override fun getMeteorShower(location: Coordinate, date: ZonedDateTime): MeteorShowerPeak? {
        val startOfDay = ZonedDateTime.of(date.toLocalDate(), LocalTime.MIN, date.zone)

        val solarLongitude = getSolarLongitude(date)

        for (shower in MeteorShower.values()) {
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
    ): RiseSetTransitTimes? {
        val time = getNextTimeAtSolarLongitude(shower.solarLongitude, now)
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

        val night = getClosestNight(
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
                val altitude = getMeteorShowerAltitude(shower, location, currentTime.toInstant())
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

    private fun getClosestNight(
        time: ZonedDateTime?,
        location: Coordinate,
        sunTimesMode: SunTimesMode
    ): Range<ZonedDateTime>? {
        if (time == null) {
            return null
        }
        val yesterday = getSunEvents(time.minusDays(1), location, sunTimesMode)
        val today = getSunEvents(time, location, sunTimesMode)
        val tomorrow = getSunEvents(time.plusDays(1), location, sunTimesMode)

        if (yesterday.set == null || today.rise == null || today.set == null || tomorrow.rise == null) {
            return if (!isSunUp(time, location) && today.rise == null) {
                // Sun does not set
                Range(time.atStartOfDay(), time.atEndOfDay())
            } else {
                null
            }
        }

        val lastNight = Range(yesterday.set, today.rise)
        val tonight = Range(today.set, tomorrow.rise)

        val timeUntilLastNight = Duration.between(time, lastNight.end).abs()
        val timeUntilTonight = Duration.between(time, tonight.start).abs()


        return if (timeUntilLastNight < timeUntilTonight) {
            lastNight
        } else {
            tonight
        }

    }

    private fun getMeteorShowerTimes(
        shower: MeteorShower,
        location: Coordinate,
        date: ZonedDateTime
    ): RiseSetTransitTimes {
        // Purposefully use newton's method here to get the rise and set times, since missing rise/set/transit times are expected
        return NewtonsRiseSetTransitTimeCalculator().calculate(
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

    private fun getAboveHorizonTimes(
        location: Coordinate,
        time: ZonedDateTime,
        nextRiseOffset: Duration,
        isUpPredicate: (Coordinate, ZonedDateTime) -> Boolean,
        riseSetTransitTimesProducer: (Coordinate, ZonedDateTime) -> RiseSetTransitTimes
    ): Range<ZonedDateTime>? {
        // If it is up, use the last rise to the next set
        // If it is down and is less than nextRiseOffset from the next rise, use the next rise to the next set
        // If it is down and is greater than nextRiseOffset from the next rise, use the last rise to the last set
        val isUp = isUpPredicate(location, time)

        val yesterday = riseSetTransitTimesProducer(location, time.minusDays(1))
        val today = riseSetTransitTimesProducer(location, time)
        val tomorrow = riseSetTransitTimesProducer(location, time.plusDays(1))

        val lastRise =
            getClosestPastTime(time, listOfNotNull(yesterday.rise, today.rise, tomorrow.rise))
        val nextRise = getClosestFutureTime(
            time,
            listOfNotNull(yesterday.rise, today.rise, tomorrow.rise)
        )
        val lastSet =
            getClosestPastTime(time, listOfNotNull(yesterday.set, today.set, tomorrow.set))
        val nextSet =
            getClosestFutureTime(time, listOfNotNull(yesterday.set, today.set, tomorrow.set))

        if (isUp) {
            return Range(lastRise ?: time.atStartOfDay(), nextSet ?: time.atEndOfDay())
        }

        if (nextRise == null || Duration.between(time, nextRise) > nextRiseOffset) {
            if (lastRise == null && lastSet == null) {
                return null
            }

            return Range(lastRise ?: time.atStartOfDay(), lastSet ?: time.atEndOfDay())
        }

        return Range(nextRise, nextSet ?: time.atEndOfDay())
    }

    override fun getStarAltitude(
        star: Star,
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean
    ): Float {
        return AstroUtils.getAltitude(
            StarLocator(star),
            time.toUniversalTime(),
            location,
            withRefraction = withRefraction
        )
    }

    override fun getStarAzimuth(
        star: Star,
        time: ZonedDateTime,
        location: Coordinate
    ): Bearing {
        return AstroUtils.getAzimuth(StarLocator(star), time.toUniversalTime(), location)
    }

    override fun getColorTemperature(star: Star): Float {
        return 4600f * ((1 / (0.92f * star.colorIndexBV + 1.7f)) + (1 / (0.92f * star.colorIndexBV + 0.62f)))
    }

    override fun plateSolve(
        readings: List<AltitudeAzimuth>,
        time: ZonedDateTime,
        approximateLocation: Coordinate?
    ): List<Pair<AltitudeAzimuth, Star>> {
        return PlateSolver().solve(readings, time, approximateLocation ?: Time.getLocationFromTimeZone(time.zone))
    }

    override fun getZenithDistance(altitude: Float): Distance {
        val zenith = 90f - altitude
        return Distance.nauticalMiles(zenith * 60)
    }

    override fun getLocationFromStars(
        starReadings: List<StarReading>,
        approximateLocation: Coordinate?
    ): Coordinate? {
        return StarLocationCalculator().getLocationFromStars(starReadings, approximateLocation)
    }

}