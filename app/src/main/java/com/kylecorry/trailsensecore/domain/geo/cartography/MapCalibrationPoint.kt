package com.kylecorry.trailsensecore.domain.geo.cartography

import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.domain.pixels.PercentCoordinate

data class MapCalibrationPoint(val location: Coordinate, val imageLocation: PercentCoordinate)
