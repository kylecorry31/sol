package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.units.Distance

interface ICelestialNavigationService {
    fun getZenithDistance(altitude: Float): Distance
}