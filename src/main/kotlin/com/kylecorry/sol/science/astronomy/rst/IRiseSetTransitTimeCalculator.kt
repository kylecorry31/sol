package com.kylecorry.sol.science.astronomy.rst

import com.kylecorry.sol.science.astronomy.RiseSetTransitTimes
import com.kylecorry.sol.science.astronomy.locators.ICelestialLocator
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.time.ZonedDateTime

internal interface IRiseSetTransitTimeCalculator {
    fun calculate(
        locator: ICelestialLocator,
        date: ZonedDateTime,
        location: Coordinate,
        standardAltitude: Double = 0.0,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): RiseSetTransitTimes
}