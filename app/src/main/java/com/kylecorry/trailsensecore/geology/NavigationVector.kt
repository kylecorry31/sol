package com.kylecorry.trailsensecore.geology

import com.kylecorry.andromeda.core.units.Bearing

data class NavigationVector(val direction: Bearing, val distance: Float, val altitudeChange: Float? = null)