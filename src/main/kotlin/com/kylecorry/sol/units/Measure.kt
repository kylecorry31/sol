package com.kylecorry.sol.units

internal typealias Measure = Long

internal fun measureValue(bytes: Measure): Float {
    return Float.fromBits((bytes and 0xFFFFFFFF).toInt())
}

internal fun measureUnitOrdinal(bytes: Measure): Int {
    return (bytes shr 32).toInt()
}

internal inline fun <reified T : Enum<T>> measureUnit(bytes: Measure): T {
    val ordinal = measureUnitOrdinal(bytes)
    return enumValues<T>()[ordinal]
}

internal fun <T : Enum<T>> packMeasure(value: Float, unit: T): Measure {
    return (value.toBits().toLong() and 0xFFFFFFFF) or ((unit.ordinal.toLong() and 0xFFFFFFFF) shl 32)
}


@JvmInline
value class PressureMeasure private constructor(private val measure: Measure) {
    val value: Float
        get() = measureValue(measure)

    val unit: PressureUnits
        get() = measureUnit<PressureUnits>(measure)

    companion object {
        fun from(value: Float, unit: PressureUnits): PressureMeasure {
            return PressureMeasure(packMeasure(value, unit))
        }
    }
}

@JvmInline
value class TemperatureMeasure private constructor(private val measure: Measure) {
    val value: Float
        get() = measureValue(measure)

    val unit: TemperatureUnits
        get() = measureUnit<TemperatureUnits>(measure)

    companion object {
        fun from(value: Float, unit: TemperatureUnits): TemperatureMeasure {
            return TemperatureMeasure(packMeasure(value, unit))
        }
    }
}

@JvmInline
value class VolumeMeasure private constructor(private val measure: Measure) {
    val value: Float
        get() = measureValue(measure)

    val unit: VolumeUnits
        get() = measureUnit<VolumeUnits>(measure)

    companion object {
        fun from(value: Float, unit: VolumeUnits): VolumeMeasure {
            return VolumeMeasure(packMeasure(value, unit))
        }
    }
}

@JvmInline
value class WeightMeasure private constructor(private val measure: Measure) {
    val value: Float
        get() = measureValue(measure)

    val unit: WeightUnits
        get() = measureUnit<WeightUnits>(measure)

    companion object {
        fun from(value: Float, unit: WeightUnits): WeightMeasure {
            return WeightMeasure(packMeasure(value, unit))
        }
    }
}

@JvmInline
value class EnergyMeasure private constructor(private val measure: Measure) {
    val value: Float
        get() = measureValue(measure)

    val unit: EnergyUnits
        get() = measureUnit<EnergyUnits>(measure)

    companion object {
        fun from(value: Float, unit: EnergyUnits): EnergyMeasure {
            return EnergyMeasure(packMeasure(value, unit))
        }
    }
}