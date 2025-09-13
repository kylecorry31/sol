package com.kylecorry.sol.science.geology

import com.kylecorry.sol.units.Angle
import com.kylecorry.sol.units.Distance

data class NavigationVector(val direction: Angle, val distance: Distance, val altitudeChange: Distance? = null)