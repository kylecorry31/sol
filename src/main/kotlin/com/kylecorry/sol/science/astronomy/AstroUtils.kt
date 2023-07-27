package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.science.astronomy.locators.ICelestialLocator
import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.units.HorizonCoordinate
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance

// Algorithms from Jean Meeus (Astronomical Algorithms 2nd Edition)
internal object AstroUtils {

    fun getAltitude(
        locator: ICelestialLocator,
        ut: UniversalTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Float {
        return getLocation(
            locator.getCoordinates(ut),
            ut,
            location,
            withRefraction,
            if (withParallax) locator.getDistance(ut) else null
        ).altitude.toFloat()
    }

    fun getAltitude(
        coordinate: EquatorialCoordinate,
        ut: UniversalTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        distanceToBody: Distance? = null
    ): Float {
        return getLocation(coordinate, ut, location, withRefraction, distanceToBody).altitude.toFloat()
    }

    fun getAzimuth(
        locator: ICelestialLocator,
        ut: UniversalTime,
        location: Coordinate,
        withParallax: Boolean = false
    ): Bearing {
        val azimuth = getLocation(
            locator.getCoordinates(ut),
            ut,
            location,
            false,
            if (withParallax) locator.getDistance(ut) else null
        ).azimuth.toFloat()
        return Bearing(azimuth)
    }

    fun getAzimuth(
        coordinate: EquatorialCoordinate,
        ut: UniversalTime,
        location: Coordinate,
        distanceToBody: Distance? = null
    ): Bearing {
        val azimuth = getLocation(coordinate, ut, location, false, distanceToBody).azimuth.toFloat()
        return Bearing(azimuth)
    }

    fun getLocation(
        locator: ICelestialLocator,
        ut: UniversalTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): HorizonCoordinate {
        val coords = locator.getCoordinates(ut)
        val distance = if (withParallax) locator.getDistance(ut) else null
        return getLocation(
            coords,
            ut,
            location,
            withRefraction,
            distance
        )
    }

    private fun getLocation(
        coordinate: EquatorialCoordinate,
        ut: UniversalTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        distanceToBody: Distance? = null
    ): HorizonCoordinate {
        val horizon = if (distanceToBody != null) {
            HorizonCoordinate.fromEquatorial(coordinate, ut, location, distanceToBody)
        } else {
            HorizonCoordinate.fromEquatorial(coordinate, ut, location)
        }
        return if (withRefraction) {
            horizon.withRefraction()
        } else {
            horizon
        }
    }
}