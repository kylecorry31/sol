package com.kylecorry.trailsensecore.domain.navigation

import com.kylecorry.trailsensecore.domain.geo.Bearing

data class NavigationVector(val direction: Bearing, val distance: Float, val altitudeChange: Float? = null)