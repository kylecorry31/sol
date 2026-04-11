package com.kylecorry.sol.science.geology

import com.kylecorry.sol.units.Bearing

data class NavigationVector(val direction: Bearing, val distance: Float, val altitudeChange: Float? = null)