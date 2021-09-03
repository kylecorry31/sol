package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.locators.ICelestialLocator
import com.kylecorry.trailsensecore.domain.astronomy.units.fromJulianDay
import java.time.ZonedDateTime

class RiseSetTransitTimeCalculator {

    fun calculate(
        locator: ICelestialLocator,
        date: ZonedDateTime,
        location: Coordinate,
        standardAltitude: Double = 0.0,
        withRefraction: Boolean = false
    ): RiseSetTransitTimes {

        return Astro.getTransitEvents(
            date,
            location,
            standardAltitude,
            withRefraction
        ) {
            val ut = fromJulianDay(it)
            val coords = locator.getCoordinates(ut)
            AstroCoordinates(coords.declination, coords.rightAscension)
        }
    }

}