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
import com.kylecorry.sol.science.astronomy.locators.*
import com.kylecorry.sol.science.astronomy.meteors.MeteorShower
import com.kylecorry.sol.science.astronomy.meteors.MeteorShowerPeak
import com.kylecorry.sol.science.astronomy.moon.MoonPhase
import com.kylecorry.sol.science.astronomy.moon.MoonTruePhase
import com.kylecorry.sol.science.astronomy.rst.NewtonsRiseSetTransitTimeCalculator
import com.kylecorry.sol.science.astronomy.rst.RobustRiseSetTransitTimeCalculator
import com.kylecorry.sol.science.astronomy.stars.*
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

object Astronomy {

    private val sun = Sun()
    private val moon = Moon()
    private val radiation = SolarRadiationCalculator()
    private val riseSetTransitCalculator = RobustRiseSetTransitTimeCalculator()

    fun getSunEvents(
        date: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
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

    fun getSunAltitude(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Float {
        return AstroUtils.getAltitude(
            sun,
            time.toUniversalTime(),
            location,
            withRefraction,
            withParallax
        )
    }

    fun getSunAzimuth(
        time: ZonedDateTime,
        location: Coordinate,
        withParallax: Boolean = false
    ): Bearing {
        return AstroUtils.getAzimuth(sun, time.toUniversalTime(), location, withParallax)
    }

    fun getNextSunset(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
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

    fun getNextSunrise(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
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

    fun isSunUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Boolean {
        return getSunAltitude(time, location, withRefraction, withParallax) > 0
    }

    fun getDaylightLength(
        date: ZonedDateTime,
        location: Coordinate,
        sunTimesMode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
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

    fun getSunDistance(time: ZonedDateTime): Distance {
        return sun.getDistance(time.toUniversalTime())
    }

    /**
     * Gets the solar radiation for the given time in kW/m^2
     */
    fun getSolarRadiation(
        date: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Double {
        return radiation.getRadiation(
            date.toUniversalTime(),
            location,
            withRefraction = withRefraction,
            withParallax = withParallax
        )
    }

    /**
     * Gets the solar radiation for the given time in kW/m^2
     */
    fun getSolarRadiation(
        date: ZonedDateTime,
        location: Coordinate,
        tilt: Float,
        azimuth: Bearing,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
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

    /**
     * Gets the times the sun is above the horizon within approximately a day.
     * If the sun does not set, it will return from the last rise (or start of day) until the end of the day.
     * @param location The location of the observer
     * @param time The current time
     * @param nextRiseOffset The duration before the next rise to switch to the next day's times
     * @param mode The mode to use for calculating sun times
     * @param withRefraction True to correct for atmospheric refraction
     * @param withParallax True to correct for parallax
     * @return The range of times the sun is above the horizon or null if it is not above the horizon within approximately a day.
     */
    fun getSunAboveHorizonTimes(
        location: Coordinate,
        time: ZonedDateTime,
        nextRiseOffset: Duration = Duration.ofHours(6),
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Range<ZonedDateTime>? {
        return getAboveHorizonTimes(
            location,
            time,
            nextRiseOffset,
            { loc, t -> isSunUp(t, loc, withRefraction, withParallax) },
            { loc, t -> getSunEvents(t, loc, mode, withRefraction, withParallax) }
        )
    }

    fun getMoonEvents(
        date: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
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

    fun getMoonAltitude(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Float {
        return AstroUtils.getAltitude(
            moon,
            time.toUniversalTime(),
            location,
            withRefraction,
            withParallax
        )
    }

    fun getMoonAzimuth(
        time: ZonedDateTime,
        location: Coordinate,
        withParallax: Boolean = false
    ): Bearing {
        return AstroUtils.getAzimuth(moon, time.toUniversalTime(), location, withParallax)
    }

    fun getNextMoonset(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
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

    fun getNextMoonrise(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
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

    fun getMoonPhase(date: ZonedDateTime): MoonPhase {
        return moon.getPhase(date.toUniversalTime())
    }

    fun isMoonUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Boolean {
        return getMoonAltitude(time, location, withRefraction, withParallax) > 0
    }

    fun getMoonDistance(time: ZonedDateTime): Distance {
        return moon.getDistance(time.toUniversalTime())
    }

    fun isSuperMoon(time: ZonedDateTime): Boolean {
        val phase = getMoonPhase(time)
        if (phase.phase != MoonTruePhase.Full) {
            return false
        }
        val distance = getMoonDistance(time)
        return distance.convertTo(DistanceUnits.Kilometers).value <= 360000f
    }

    /**
     * Gets the times the moon is above the horizon within approximately a day.
     * If the sun does not set, it will return from the last rise (or start of day) until the end of the day.
     * @param location The location of the observer
     * @param time The current time
     * @param nextRiseOffset The duration before the next rise to switch to the next day's times
     * @param withRefraction True to correct for atmospheric refraction
     * @param withParallax True to correct for parallax
     * @return The range of times the moon is above the horizon or null if it is not above the horizon within approximately a day.
     */
    fun getMoonAboveHorizonTimes(
        location: Coordinate,
        time: ZonedDateTime,
        nextRiseOffset: Duration = Duration.ofHours(6),
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Range<ZonedDateTime>? {
        return getAboveHorizonTimes(
            location,
            time,
            Duration.ofHours(6),
            { loc, t -> isMoonUp(t, loc, withRefraction, withParallax) },
            { loc, t -> getMoonEvents(t, loc, withRefraction, withParallax) }
        )
    }

    /**
     * The tilt of the illuminated fraction of the moon in degrees clockwise from the top of the moon.
     */
    fun getMoonTilt(time: ZonedDateTime, location: Coordinate): Float {
        return moon.getTilt(time.toUniversalTime(), location)
    }

    /**
     * The parallactic angle of the moon in degrees.
     */
    fun getMoonParallacticAngle(time: ZonedDateTime, location: Coordinate): Float {
        return AstroUtils.getParallacticAngle(moon, time.toUniversalTime(), location)
    }

    fun getSeason(location: Coordinate, date: ZonedDateTime): Season {
        val sl = wrap(getSolarLongitude(date), 0f, 360f)
        return when {
            sl >= OrbitalPosition.WinterSolstice.solarLongitude -> if (location.isNorthernHemisphere) Season.Winter else Season.Summer
            sl >= OrbitalPosition.AutumnalEquinox.solarLongitude -> if (location.isNorthernHemisphere) Season.Fall else Season.Spring
            sl >= OrbitalPosition.SummerSolstice.solarLongitude -> if (location.isNorthernHemisphere) Season.Summer else Season.Winter
            else -> if (location.isNorthernHemisphere) Season.Spring else Season.Fall
        }
    }

    fun getNextEclipse(
        time: ZonedDateTime,
        location: Coordinate,
        type: EclipseType,
        maxSearch: Duration? = null
    ): Eclipse? {
        // TODO: Apply max search to lunar as well
        val calculator = when (type) {
            EclipseType.PartialLunar -> PartialLunarEclipseCalculator()
            EclipseType.TotalLunar -> TotalLunarEclipseCalculator()
            EclipseType.Solar -> SolarEclipseCalculator(maxDuration = maxSearch)
        }
        return calculator.getNextEclipse(time.toInstant(), location)
    }

    fun getEclipseMagnitude(time: ZonedDateTime, location: Coordinate, type: EclipseType): Float? {
        val calculator = when (type) {
            EclipseType.PartialLunar -> PartialLunarEclipseCalculator()
            EclipseType.TotalLunar -> TotalLunarEclipseCalculator()
            EclipseType.Solar -> SolarEclipseCalculator()
        }

        return calculator.getMagnitude(time.toInstant(), location)
    }

    fun getEclipseObscuration(time: ZonedDateTime, location: Coordinate, type: EclipseType): Float? {
        val calculator = when (type) {
            EclipseType.PartialLunar -> PartialLunarEclipseCalculator()
            EclipseType.TotalLunar -> TotalLunarEclipseCalculator()
            EclipseType.Solar -> SolarEclipseCalculator()
        }

        return calculator.getObscuration(time.toInstant(), location)
    }

    fun getMeteorShower(location: Coordinate, date: ZonedDateTime): MeteorShowerPeak? {
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

    fun getMeteorShowerAltitude(
        shower: MeteorShower,
        location: Coordinate,
        time: Instant
    ): Float {
        val locator = MeteorShowerLocator(shower)
        return AstroUtils.getAltitude(locator, time.toUniversalTime(), location, false)
    }

    fun getMeteorShowerAzimuth(
        shower: MeteorShower,
        location: Coordinate,
        time: Instant
    ): Bearing {
        val locator = MeteorShowerLocator(shower)
        return AstroUtils.getAzimuth(locator, time.toUniversalTime(), location)
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

    fun getStarAltitude(
        star: Star,
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false
    ): Float {
        return AstroUtils.getAltitude(
            StarLocator(star),
            time.toUniversalTime(),
            location,
            withRefraction = withRefraction
        )
    }

    fun getStarAzimuth(
        star: Star,
        time: ZonedDateTime,
        location: Coordinate
    ): Bearing {
        return AstroUtils.getAzimuth(StarLocator(star), time.toUniversalTime(), location)
    }

    /**
     * Get the color temperature of a star in Kelvin
     */
    fun getColorTemperature(star: Star): Float {
        return 4600f * ((1 / (0.92f * star.colorIndexBV + 1.7f)) + (1 / (0.92f * star.colorIndexBV + 0.62f)))
    }

    /**
     * Matches the readings to stars
     */
    fun plateSolve(
        readings: List<AltitudeAzimuth>,
        time: ZonedDateTime,
        approximateLocation: Coordinate? = null,
        tolerance: Float = 0.04f,
        minMatches: Int = 5,
        numNeighbors: Int = 3,
        minMagnitude: Float = 4f
    ): List<DetectedStar> {
        return PlateSolver(tolerance, minMatches, numNeighbors, minMagnitude).solve(
            readings,
            time,
            approximateLocation ?: Time.getLocationFromTimeZone(time.zone)
        )
    }

    fun getZenithDistance(altitude: Float): Distance {
        val zenith = 90f - altitude
        return Distance.nauticalMiles(zenith * 60)
    }

    fun getLocationFromStars(
        starReadings: List<StarReading>,
        approximateLocation: Coordinate? = null
    ): Coordinate? {
        return StarLocationCalculator().getLocationFromStars(starReadings, approximateLocation)
    }

    fun getPlanetPosition(
        planet: Planet,
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): CelestialObservation {
        val ut = time.toUniversalTime()
        val locator = PlanetLocator(planet)
        val horizonCoordinate = AstroUtils.getLocation(
            locator,
            ut,
            location,
            withRefraction,
            withParallax
        )
        val distance = locator.getDistance(ut)
        val diameter = locator.getAngularDiameter(ut)
        val magnitude = locator.getMagnitude(ut)
        return CelestialObservation(
            Bearing.from(horizonCoordinate.azimuth.toFloat()),
            horizonCoordinate.altitude.toFloat(),
            diameter.toFloat(),
            magnitude.toFloat(),
            distance
        )
    }

    fun getPlanetEvents(
        planet: Planet,
        date: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): RiseSetTransitTimes {
        return riseSetTransitCalculator.calculate(
            PlanetLocator(planet),
            date,
            location,
            0.0,
            withRefraction,
            withParallax
        )
    }

}