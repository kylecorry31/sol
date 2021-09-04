package com.kylecorry.sol.science.astronomy.locators

import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.units.UniversalTime

internal interface ICelestialLocator {
    fun getCoordinates(ut: UniversalTime): EquatorialCoordinate
}