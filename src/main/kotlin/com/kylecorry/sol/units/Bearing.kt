package com.kylecorry.sol.units

import com.kylecorry.sol.math.trigonometry.Trigonometry.normalizeAngle

@JvmInline
value class Bearing private constructor(val value: Float) {
    val direction: CompassDirection
        get() = CompassDirection.nearest(value)

    val mils: Float
        get() = value * 17.77778f

    fun withDeclination(declination: Float): Bearing {
        return from(value + declination)
    }

    fun inverse(): Bearing {
        return from(value + 180)
    }

    companion object {
        fun from(direction: CompassDirection): Bearing {
            return from(direction.azimuth)
        }

        fun from(value: Float): Bearing {
            return Bearing(if (value.isNaN() || !value.isFinite()) 0f else normalizeAngle(value))
        }

        fun getBearing(degrees: Float): Float {
            return if (degrees.isNaN() || !degrees.isFinite()) 0f else normalizeAngle(degrees)
        }
    }
}