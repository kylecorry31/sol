package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.science.astronomy.locators.ICelestialLocator
import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.units.HorizonCoordinate
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate

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
        return getAzimuth(coords, ut, location)
    }

    fun getAzimuth(
        coordinate: EquatorialCoordinate,
        ut: UniversalTime,
        location: Coordinate
    ): Bearing {
        val horizon = HorizonCoordinate.fromEquatorial(coordinate, ut, location)
        return Bearing(horizon.azimuth.toFloat())
    }
}