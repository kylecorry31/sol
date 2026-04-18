package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.science.astronomy.locators.StarLocator
import com.kylecorry.sol.science.astronomy.stars.AltitudeAzimuth
import com.kylecorry.sol.science.astronomy.stars.DetectedStar
import com.kylecorry.sol.science.astronomy.stars.PlateSolver
import com.kylecorry.sol.science.astronomy.stars.Star
import com.kylecorry.sol.science.astronomy.stars.StarLocationCalculator
import com.kylecorry.sol.science.astronomy.stars.StarReading
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.time.Time
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import java.time.ZonedDateTime

internal object StarFacade {
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
}
