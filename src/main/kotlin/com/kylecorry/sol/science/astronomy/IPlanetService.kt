package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.science.astronomy.locators.Planet
import com.kylecorry.sol.science.astronomy.units.CelestialObservation
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.time.ZonedDateTime

interface IPlanetService {
    fun getPlanetPosition(
        planet: Planet,
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): CelestialObservation

    fun getPlanetEvents(
        planet: Planet,
        date: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): RiseSetTransitTimes
}