package com.kylecorry.trailsensecore.infrastructure.gpx

import com.kylecorry.trailsensecore.domain.geo.Coordinate
import java.time.Instant

data class GPXWaypoint(val coordinate: Coordinate, val name: String?, val elevation: Float?, val comment: String?, val time: Instant?, val group: String?)
