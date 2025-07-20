package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.science.astronomy.locators.Planet
import com.kylecorry.sol.science.astronomy.units.CelestialPosition
import com.kylecorry.sol.units.Coordinate
import java.time.ZonedDateTime

interface IPlanetService {
    fun getPlanetPosition(
        planet: Planet,
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): CelestialPosition

    fun getPlanetEvents(
        planet: Planet,
        date: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): RiseSetTransitTimes
}