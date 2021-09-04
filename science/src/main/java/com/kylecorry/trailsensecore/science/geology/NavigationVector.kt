package com.kylecorry.trailsensecore.science.geology

import com.kylecorry.andromeda.core.units.Bearing

data class NavigationVector(val direction: Bearing, val distance: Float, val altitudeChange: Float? = null)