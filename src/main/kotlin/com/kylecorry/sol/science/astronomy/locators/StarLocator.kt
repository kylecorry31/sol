package com.kylecorry.sol.science.astronomy.locators

import com.kylecorry.sol.science.astronomy.stars.Star
import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.units.Distance

internal class StarLocator(private val star: Star) : ICelestialLocator {
    override fun getCoordinates(ut: UniversalTime): EquatorialCoordinate {
        return star.coordinate
    }

    override fun getDistance(ut: UniversalTime): Distance? {
        return null
    }
}