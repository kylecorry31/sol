package com.kylecorry.sol.units

@JvmInline
value class Volume private constructor(private val measure: Measure) {
    val value: Float
        get() = measureValue(measure)

    val units: VolumeUnits
        get() = measureUnit<VolumeUnits>(measure)

    fun convertTo(newUnits: VolumeUnits): Volume {
        if (units == newUnits) {
            return this
        }
        val l = value * units.liters
        return from((l / newUnits.liters).toFloat(), newUnits)
    }

    override fun toString(): String {
        return "$value $units"
    }

    companion object {
        fun from(value: Float, unit: VolumeUnits): Volume {
            return Volume(packMeasure(value, unit))
        }
    }
}