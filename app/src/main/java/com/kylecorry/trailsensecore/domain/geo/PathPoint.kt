package com.kylecorry.trailsensecore.domain.geo

import com.kylecorry.trailsensecore.domain.network.CellNetworkQuality
import java.time.Instant

data class PathPoint(
    val id: Long,
    val pathId: Long,
    val coordinate: Coordinate,
    val elevation: Float? = null,
    val time: Instant? = null,
    val cellSignal: CellNetworkQuality? = null
)