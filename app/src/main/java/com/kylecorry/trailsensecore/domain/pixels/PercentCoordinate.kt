package com.kylecorry.trailsensecore.domain.pixels

import com.kylecorry.andromeda.core.units.PixelCoordinate

data class PercentCoordinate(val x: Float, val y: Float) {
    fun toPixels(width: Float, height: Float): PixelCoordinate {
        return PixelCoordinate(x * width, y * height)
    }
}
