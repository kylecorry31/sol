package com.kylecorry.trailsensecore.science.meteorology

import java.time.Instant

data class PressureReading(val time: Instant, val value: Float) {
    fun isHigh(): Boolean = value >= 1022.689
    fun isLow(): Boolean = value <= 1009.144
}