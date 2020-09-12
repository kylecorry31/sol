package com.kylecorry.trailsensecore.domain.navigation

import com.kylecorry.trailsensecore.domain.Bearing
import com.kylecorry.trailsensecore.domain.Coordinate

data class Position(val location: Coordinate, val altitude: Float, val bearing: Bearing, val speed: Float = 0f)