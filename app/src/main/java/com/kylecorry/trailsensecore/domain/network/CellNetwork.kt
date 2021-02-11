package com.kylecorry.trailsensecore.domain.network

enum class CellNetwork(val minDbm: Int, val maxDbm: Int) {
    Nr(-140, -44),
    Lte(-140, -44),
    Cdma(-100, -75),
    Wcdma(-113, -51),
    Gsm(-113, -51)
}