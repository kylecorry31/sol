package com.kylecorry.sol.units

import kotlin.math.absoluteValue

@JvmInline
value class Weight private constructor(private val measure: Measure) {
    val value: Float
        get() = measureValue(measure)

    val units: WeightUnits
        get() = measureUnit<WeightUnits>(measure)

    fun convertTo(newUnits: WeightUnits): Weight {
        if (units == newUnits) {
            return this
        }
        val grams = value * units.grams
        return from((grams / newUnits.grams).toFloat(), newUnits)
    }

    operator fun plus(other: Weight): Weight {
        val otherInUnits = other.convertTo(units)
        return from(value + otherInUnits.value, units)
    }

    operator fun times(amount: Number): Weight {
        return from(value * amount.toFloat().absoluteValue, units)
    }

    override fun toString(): String {
        return "$value $units"
    }

    companion object {
        fun from(value: Float, units: WeightUnits): Weight {
            return Weight(packMeasure(value, units))
        }
    }
}
