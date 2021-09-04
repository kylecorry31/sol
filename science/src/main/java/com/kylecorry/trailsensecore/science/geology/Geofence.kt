package com.kylecorry.trailsensecore.science.geology

import com.kylecorry.trailsensecore.units.Coordinate
import com.kylecorry.trailsensecore.units.Distance

data class Geofence(val center: Coordinate, val radius: Distance) {
    fun contains(coordinate: Coordinate): Boolean {
        val distanceToCenter = coordinate.distanceTo(center)
        return distanceToCenter <= radius.meters().distance
    }
}