package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.locators.ICelestialLocator
import com.kylecorry.trailsensecore.domain.astronomy.units.EquatorialCoordinate
import com.kylecorry.trailsensecore.domain.astronomy.units.HorizonCoordinate
import com.kylecorry.trailsensecore.domain.astronomy.units.UniversalTime

// Algorithms from Jean Meeus (Astronomical Algorithms 2nd Edition)
internal object AstroUtils {

    fun getAltitude(
        locator: ICelestialLocator,
        ut: UniversalTime,
        location: Coordinate,
        withRefraction: Boolean = false
    ): Float {
        return getAltitude(locator.getCoordinates(ut), ut, location, withRefraction)
    }

    fun getAltitude(
        coordinate: EquatorialCoordinate,
        ut: UniversalTime,
        location: Coordinate,
        withRefraction: Boolean = false
    ): Float {
        val horizon = HorizonCoordinate.fromEquatorial(coordinate, ut, location)
        return horizon.let {
            if (withRefraction) {
                it.withRefraction()
            } else {
                it
            }
        }.altitude.toFloat()
    }

    fun getAzimuth(
        locator: ICelestialLocator,
        ut: UniversalTime,
        location: Coordinate
    ): Bearing {
        val coords = locator.getCoordinates(ut)
        val horizon = HorizonCoordinate.fromEquatorial(coords, ut, location)
        return Bearing(horizon.azimuth.toFloat())
    }
}