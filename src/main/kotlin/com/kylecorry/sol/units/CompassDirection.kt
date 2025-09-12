package com.kylecorry.sol.units

import com.kylecorry.sol.math.SolMath.roundNearest

enum class CompassDirection(val azimuth: Float) {
    North(0f),
    NorthEast(45f),
    East(90f),
    SouthEast(135f),
    South(180f),
    SouthWest(225f),
    West(270f),
    NorthWest(315f);

    companion object {
        fun nearest(bearing: Float): CompassDirection {
            val directions = CompassDirection.entries.toTypedArray()
            val rounded = bearing.roundNearest(45f) % 360
            return directions.firstOrNull { rounded == it.azimuth } ?: North
        }
    }
}
