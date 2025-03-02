package com.kylecorry.sol.science.astronomy.locators

import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Quantity

internal interface ICelestialLocator {
    fun getCoordinates(ut: UniversalTime): EquatorialCoordinate
    fun getDistance(ut: UniversalTime): Quantity<Distance>?
}