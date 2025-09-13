package com.kylecorry.sol.units

import com.kylecorry.sol.math.SolMath.deltaAngle
import com.kylecorry.sol.math.SolMath.normalizeAngle
import com.kylecorry.sol.math.SolMath.wrap
import kotlin.math.PI

@JvmInline
value class Angle private constructor(private val measure: Measure) {
    val value: Float
        get() = measureValue(measure)

    val units: AngleUnits
        get() = measureUnit<AngleUnits>(measure)

    fun convertTo(toUnits: AngleUnits): Angle {
        if (units == toUnits) {
            return this
        }
        val rad = value * units.radians
        val newAngle = rad / toUnits.radians
        return from(newAngle, toUnits)
    }

    fun radians(): Angle {
        return convertTo(AngleUnits.Radians)
    }

    fun degrees(): Angle {
        return convertTo(AngleUnits.Degrees)
    }

    fun normalized(): Angle {
        val normalRadians = radians(radians().value, true)
        return normalRadians.convertTo(units)
    }

    fun inverse(): Angle {
        val radians = radians().value
        val inverseRadians = radians + maxRadians / 2f
        val newAngle = radians(inverseRadians, true)
        return newAngle.convertTo(units)
    }

    fun angleTo(other: Angle): Angle {
        val degrees1 = degrees().value
        val degrees2 = other.degrees().value
        val delta = deltaAngle(degrees1, degrees2)
        return degrees(delta).convertTo(units)
    }

    fun sin(): Float {
        return kotlin.math.sin(radians().value)
    }

    fun cos(): Float {
        return kotlin.math.cos(radians().value)
    }

    fun tan(): Float {
        return kotlin.math.tan(radians().value)
    }

    operator fun plus(other: Angle): Angle {
        return from(value + other.convertTo(units).value, units)
    }

    companion object {
        private const val maxRadians = 2 * PI.toFloat()

        fun from(value: Float, units: AngleUnits): Angle {
            return Angle(packMeasure(value, units))
        }

        fun radians(angle: Float, normalize: Boolean = false): Angle {
            return from(if (normalize) wrap(angle, 0f, maxRadians) % maxRadians else angle, AngleUnits.Radians)
        }

        fun degrees(angle: Float, normalize: Boolean = false): Angle {
            return from(if (normalize) normalizeAngle(angle) else angle, AngleUnits.Degrees)
        }
    }
}