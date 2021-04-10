package com.kylecorry.trailsensecore.domain.pixels

data class PixelCircle(val center: PixelCoordinate, val radius: Float){
    fun contains(pixel: PixelCoordinate): Boolean {
        val distance = center.distanceTo(pixel)
        return distance <= radius
    }
}
