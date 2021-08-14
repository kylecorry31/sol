package com.kylecorry.trailsensecore.domain.geo.cartography

import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.domain.pixels.PercentCoordinate

data class MapCalibrationPoint(val location: Coordinate, val imageLocation: PercentCoordinate)
