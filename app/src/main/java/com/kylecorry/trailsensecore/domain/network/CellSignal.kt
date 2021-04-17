package com.kylecorry.trailsensecore.domain.network

import com.kylecorry.trailsensecore.domain.units.Quality

data class CellSignal(
    val id: String,
    val strength: Float,
    val dbm: Int,
    val quality: Quality,
    val network: CellNetwork
)

data class CellNetworkQuality(
    val network: CellNetwork,
    val quality: Quality
)



