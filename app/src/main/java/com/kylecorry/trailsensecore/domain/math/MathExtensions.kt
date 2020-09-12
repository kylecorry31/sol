package com.kylecorry.trailsensecore.domain.math

import kotlin.math.*

fun normalizeAngle(angle: Float): Float {
    var outputAngle = angle
    while (outputAngle < 0) outputAngle += 360
    return outputAngle % 360
}

fun normalizeAngle(angle: Double): Double {
    var outputAngle = angle
    while (outputAngle < 0) outputAngle += 360
    return outputAngle % 360
}

fun sinDegrees(angle: Double): Double {
    return sin(angle.toRadians())
}

fun tanDegrees(angle: Double): Double {
    return tan(angle.toRadians())
}

fun tanDegrees(angle: Float): Float {
    return tan(angle.toRadians())
}

fun cosDegrees(angle: Double): Double {
    return cos(angle.toRadians())
}

fun Double.toRadians(): Double {
    return Math.toRadians(this)
}

fun Float.toRadians(): Float {
    return Math.toRadians(this.toDouble()).toFloat()
}

fun deltaAngle(angle1: Float, angle2: Float): Float {
    var delta = angle2 - angle1
    delta += 180
    delta -= floor(delta / 360) * 360
    delta -= 180
    if (abs(abs(delta) - 180) <= Float.MIN_VALUE) {
        delta = 180f
    }
    return delta
}

fun clamp(value: Float, minimum: Float, maximum: Float): Float {
    return min(maximum, max(minimum, value))
}

fun Double.roundPlaces(places: Int): Double {
    return (this * 10.0.pow(places)).roundToInt() / 10.0.pow(places)
}

fun Float.roundPlaces(places: Int): Float {
    return (this * 10f.pow(places)).roundToInt() / 10f.pow(places)
}

fun Float.toDegrees(): Float {
    return Math.toDegrees(this.toDouble()).toFloat()
}

fun Double.toDegrees(): Double {
    return Math.toDegrees(this)
}

