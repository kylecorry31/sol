package com.kylecorry.sol.science.geology

import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance

data class Geofence(val center: Coordinate, val radius: Distance) {
    fun contains(coordinate: Coordinate): Boolean {
        val distanceToCenter = coordinate.distanceTo(center)
        return distanceToCenter <= radius.meters().distance
    }
}