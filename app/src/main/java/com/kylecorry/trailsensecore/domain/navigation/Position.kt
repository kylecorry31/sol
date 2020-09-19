package com.kylecorry.trailsensecore.domain.navigation

import com.kylecorry.trailsensecore.domain.geo.Bearing
import com.kylecorry.trailsensecore.domain.geo.Coordinate

data class Position(val location: Coordinate, val altitude: Float, val bearing: Bearing, val speed: Float = 0f)