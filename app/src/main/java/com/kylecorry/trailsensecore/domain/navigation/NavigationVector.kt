package com.kylecorry.trailsensecore.domain.navigation

import com.kylecorry.andromeda.core.units.Bearing

data class NavigationVector(val direction: Bearing, val distance: Float, val altitudeChange: Float? = null)