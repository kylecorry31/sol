package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.science.astronomy.locators.Planet
import com.kylecorry.sol.science.astronomy.locators.PlanetLocator
import com.kylecorry.sol.science.astronomy.rst.RobustRiseSetTransitTimeCalculator
import com.kylecorry.sol.science.astronomy.units.CelestialObservation
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import java.time.ZonedDateTime

internal object PlanetFacade {
    private val riseSetTransitCalculator = RobustRiseSetTransitTimeCalculator()

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
