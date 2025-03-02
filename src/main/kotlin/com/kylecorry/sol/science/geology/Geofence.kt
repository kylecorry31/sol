package com.kylecorry.sol.science.geology

import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Quantity
import com.kylecorry.sol.units.meters

data class Geofence(val center: Coordinate, val radius: Quantity<Distance>): IGeoArea {
    override fun contains(location: Coordinate): Boolean {
        val distanceToCenter = location.distanceTo(center)
        return distanceToCenter <= radius.meters().amount
    }
}