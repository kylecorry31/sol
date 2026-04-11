package com.kylecorry.sol.science.astronomy.rst

import com.kylecorry.sol.science.astronomy.RiseSetTransitTimes
import com.kylecorry.sol.science.astronomy.locators.ICelestialLocator
import com.kylecorry.sol.units.Coordinate
import java.time.ZonedDateTime

internal class RobustRiseSetTransitTimeCalculator : IRiseSetTransitTimeCalculator {

    private val newtons = NewtonsRiseSetTransitTimeCalculator()
    private val search = SearchRiseSetTransitTimeCalculator()

    override fun calculate(
        locator: ICelestialLocator,
        date: ZonedDateTime,
        location: Coordinate,
        standardAltitude: Double,
        withRefraction: Boolean,
        withParallax: Boolean
    ): RiseSetTransitTimes {
        val newtonsResult = newtons.calculate(locator, date, location, standardAltitude, withRefraction, withParallax)

        if (newtonsResult.rise != null && newtonsResult.set != null && newtonsResult.transit != null) {
            return newtonsResult
        }

        // Fallback to search
        val searchResults = search.calculate(locator, date, location, standardAltitude, withRefraction, withParallax)

        return RiseSetTransitTimes(
            newtonsResult.rise ?: searchResults.rise,
            newtonsResult.transit ?: searchResults.transit,
            newtonsResult.set ?: searchResults.set
        )
    }
}