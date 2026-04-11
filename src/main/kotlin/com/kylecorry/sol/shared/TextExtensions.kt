package com.kylecorry.sol.shared

internal fun String.toDoubleCompat(): Double? {
    val asDouble = try {
        this.replace(",", ".").toDoubleOrNull()
    } catch (e: Exception) {
        null
    }
    asDouble ?: return null
    if (asDouble.isNaN() || asDouble.isInfinite()) {
        return null
    }
    return asDouble
}

internal fun String.toFloatCompat(): Float? {
    val asFloat = try {
        this.replace(",", ".").toFloatOrNull()
    } catch (e: Exception) {
        null
    }
    asFloat ?: return null
    if (asFloat.isNaN() || asFloat.isInfinite()) {
        return null
    }
    return asFloat
}