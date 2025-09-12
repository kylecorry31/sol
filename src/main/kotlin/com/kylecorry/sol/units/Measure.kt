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

internal fun measureUnit1Ordinal(bytes: Measure): Int {
    return ((bytes shr 32) and 0xFFFF).toInt()
}

internal inline fun <reified T : Enum<T>> measureUnit1(bytes: Measure): T {
    val ordinal = measureUnit1Ordinal(bytes)
    return enumValues<T>()[ordinal]
}

internal fun measureUnit2Ordinal(bytes: Measure): Int {
    return ((bytes shr 48) and 0xFFFF).toInt()
}

internal inline fun <reified T : Enum<T>> measureUnit2(bytes: Measure): T {
    val ordinal = measureUnit2Ordinal(bytes)
    return enumValues<T>()[ordinal]
}

internal fun <T : Enum<T>, R : Enum<R>> packMeasureMultiUnit(value: Float, unit1: T, unit2: R): Measure {
    return (value.toBits().toLong() and 0xFFFFFFFF) or ((unit1.ordinal.toLong() and 0xFFFF) shl 32) or
            ((unit2.ordinal.toLong() and 0xFFFF) shl 48)
}