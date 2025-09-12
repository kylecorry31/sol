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