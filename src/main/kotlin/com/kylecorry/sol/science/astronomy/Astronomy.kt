package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.astronomy.eclipse.Eclipse
import com.kylecorry.sol.science.astronomy.eclipse.EclipseType
import com.kylecorry.sol.science.astronomy.locators.Planet
import com.kylecorry.sol.science.astronomy.meteors.MeteorShower
import com.kylecorry.sol.science.astronomy.meteors.MeteorShowerPeak
import com.kylecorry.sol.science.astronomy.moon.MoonPhase
import com.kylecorry.sol.science.astronomy.stars.AltitudeAzimuth
import com.kylecorry.sol.science.astronomy.stars.DetectedStar
import com.kylecorry.sol.science.astronomy.stars.Star
import com.kylecorry.sol.science.astronomy.stars.StarReading
import com.kylecorry.sol.science.astronomy.units.CelestialObservation
import com.kylecorry.sol.science.shared.Season
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime

object Astronomy {
    fun getSunEvents(
        date: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): RiseSetTransitTimes {
        return SunFacade.getSunEvents(date, location, mode, withRefraction, withParallax)
    }

    fun getSunAltitude(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Float {
        return SunFacade.getSunAltitude(time, location, withRefraction, withParallax)
    }

    fun getSunAzimuth(
        time: ZonedDateTime,
        location: Coordinate,
        withParallax: Boolean = false
    ): Bearing {
        return SunFacade.getSunAzimuth(time, location, withParallax)
    }

    fun getNextSunset(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): ZonedDateTime? {
        return SunFacade.getNextSunset(time, location, mode, withRefraction, withParallax)
    }

    fun getNextSunrise(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): ZonedDateTime? {
        return SunFacade.getNextSunrise(time, location, mode, withRefraction, withParallax)
    }

    fun isSunUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Boolean {
        return SunFacade.isSunUp(time, location, withRefraction, withParallax)
    }

    fun getDaylightLength(
        date: ZonedDateTime,
        location: Coordinate,
        sunTimesMode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Duration {
        return SunFacade.getDaylightLength(
            date,
            location,
            sunTimesMode,
            withRefraction,
            withParallax
        )
    }

    fun getSunDistance(time: ZonedDateTime): Distance {
        return SunFacade.getSunDistance(time)
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
        return SunFacade.getSolarRadiation(date, location, withRefraction, withParallax)
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
        return SunFacade.getSolarRadiation(
            date,
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
     * @return The range of times the sun is above the horizon or null if it is not above the horizon
     * within approximately a day.
     */
    fun getSunAboveHorizonTimes(
        location: Coordinate,
        time: ZonedDateTime,
        nextRiseOffset: Duration = Duration.ofHours(6),
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Range<ZonedDateTime>? {
        return SunFacade.getSunAboveHorizonTimes(
            location,
            time,
            nextRiseOffset,
            mode,
            withRefraction,
            withParallax
        )
    }

    fun getMoonEvents(
        date: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): RiseSetTransitTimes {
        return MoonFacade.getMoonEvents(date, location, withRefraction, withParallax)
    }

    fun getMoonAltitude(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Float {
        return MoonFacade.getMoonAltitude(time, location, withRefraction, withParallax)
    }

    fun getMoonAzimuth(
        time: ZonedDateTime,
        location: Coordinate,
        withParallax: Boolean = false
    ): Bearing {
        return MoonFacade.getMoonAzimuth(time, location, withParallax)
    }

    fun getNextMoonset(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): ZonedDateTime? {
        return MoonFacade.getNextMoonset(time, location, withRefraction, withParallax)
    }

    fun getNextMoonrise(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): ZonedDateTime? {
        return MoonFacade.getNextMoonrise(time, location, withRefraction, withParallax)
    }

    fun getMoonPhase(date: ZonedDateTime): MoonPhase {
        return MoonFacade.getMoonPhase(date)
    }

    fun isMoonUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Boolean {
        return MoonFacade.isMoonUp(time, location, withRefraction, withParallax)
    }

    fun getMoonDistance(time: ZonedDateTime): Distance {
        return MoonFacade.getMoonDistance(time)
    }

    fun isSuperMoon(time: ZonedDateTime): Boolean {
        return MoonFacade.isSuperMoon(time)
    }

    /**
     * Gets the times the moon is above the horizon within approximately a day.
     * If the sun does not set, it will return from the last rise (or start of day) until the end of the day.
     * @param location The location of the observer
     * @param time The current time
     * @param nextRiseOffset The duration before the next rise to switch to the next day's times
     * @param withRefraction True to correct for atmospheric refraction
     * @param withParallax True to correct for parallax
     * @return The range of times the moon is above the horizon or null if it is not above the horizon
     * within approximately a day.
     */
    fun getMoonAboveHorizonTimes(
        location: Coordinate,
        time: ZonedDateTime,
        nextRiseOffset: Duration = Duration.ofHours(6),
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Range<ZonedDateTime>? {
        return MoonFacade.getMoonAboveHorizonTimes(
            location,
            time,
            nextRiseOffset,
            withRefraction,
            withParallax
        )
    }

    /**
     * The tilt of the illuminated fraction of the moon in degrees clockwise from the top of the moon.
     */
    fun getMoonTilt(time: ZonedDateTime, location: Coordinate): Float {
        return MoonFacade.getMoonTilt(time, location)
    }

    /**
     * The parallactic angle of the moon in degrees.
     */
    fun getMoonParallacticAngle(time: ZonedDateTime, location: Coordinate): Float {
        return MoonFacade.getMoonParallacticAngle(time, location)
    }

    fun getSeason(location: Coordinate, date: ZonedDateTime): Season {
        return SeasonFacade.getSeason(location, date)
    }

    fun getNextEclipse(
        time: ZonedDateTime,
        location: Coordinate,
        type: EclipseType,
        maxSearch: Duration? = null
    ): Eclipse? {
        return EclipseFacade.getNextEclipse(time, location, type, maxSearch)
    }

    fun getEclipseMagnitude(time: ZonedDateTime, location: Coordinate, type: EclipseType): Float? {
        return EclipseFacade.getEclipseMagnitude(time, location, type)
    }

    fun getEclipseObscuration(time: ZonedDateTime, location: Coordinate, type: EclipseType): Float? {
        return EclipseFacade.getEclipseObscuration(time, location, type)
    }

    fun getMeteorShower(location: Coordinate, date: ZonedDateTime): MeteorShowerPeak? {
        return MeteorFacade.getMeteorShower(location, date)
    }

    /**
     * Get a list of meteor showers which are active.
     * This does not check the time of day, so the shower may not currently be visible.
     */
    fun getActiveMeteorShowers(
        location: Coordinate,
        date: ZonedDateTime
    ): List<MeteorShowerPeak> {
        return MeteorFacade.getActiveMeteorShowers(location, date)
    }

    fun getMeteorShowerPosition(
        shower: MeteorShower,
        location: Coordinate,
        time: Instant
    ): CelestialObservation {
        return MeteorFacade.getMeteorShowerPosition(shower, location, time)
    }

    fun getStarPosition(
        star: Star,
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false
    ): CelestialObservation {
        return StarFacade.getStarPosition(star, time, location, withRefraction)
    }

    /**
     * Get the color temperature of a star in Kelvin
     */
    fun getColorTemperature(star: Star): Float {
        return StarFacade.getColorTemperature(star)
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
        return StarFacade.plateSolve(
            readings,
            time,
            approximateLocation,
            tolerance,
            minMatches,
            numNeighbors,
            minMagnitude
        )
    }

    fun getZenithDistance(altitude: Float): Distance {
        return StarFacade.getZenithDistance(altitude)
    }

    fun getLocationFromStars(
        starReadings: List<StarReading>,
        approximateLocation: Coordinate? = null
    ): Coordinate? {
        return StarFacade.getLocationFromStars(starReadings, approximateLocation)
    }

    fun getPlanetPosition(
        planet: Planet,
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): CelestialObservation {
        return PlanetFacade.getPlanetPosition(
            planet,
            time,
            location,
            withRefraction,
            withParallax
        )
    }

    fun getPlanetEvents(
        planet: Planet,
        date: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): RiseSetTransitTimes {
        return PlanetFacade.getPlanetEvents(planet, date, location, withRefraction, withParallax)
    }
}
