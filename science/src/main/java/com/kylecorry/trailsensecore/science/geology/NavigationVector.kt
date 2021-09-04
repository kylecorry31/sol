package com.kylecorry.trailsensecore.science.geology

import com.kylecorry.trailsensecore.units.Bearing

data class NavigationVector(val direction: Bearing, val distance: Float, val altitudeChange: Float? = null)