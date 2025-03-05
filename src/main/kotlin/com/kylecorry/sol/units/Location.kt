package com.kylecorry.sol.units

import com.kylecorry.sol.science.geology.Geology
import kotlin.math.hypot

/**
 * A location on Earth
 * @property coordinate The geographic coordinate
 * @property elevation The elevation (reference point is unspecified)
 */
class Location(val coordinate: Coordinate, val elevation: Distance) {
    fun distanceTo(other: Location, highAccuracy: Boolean = true): Float {
        val horizontal = coordinate.distanceTo(other.coordinate, highAccuracy)
        val vertical = elevation.meters().distance - other.elevation.meters().distance
        return hypot(horizontal, vertical)
    }

    fun inclinationTo(other: Location): Float {
        val distance = distanceTo(other)
        val vertical = verticalDistanceTo(other)
        return Geology.getInclination(Distance.meters(distance), Distance.meters(vertical))
    }

    fun horizontalDistanceTo(other: Location, highAccuracy: Boolean = true): Float {
        return coordinate.distanceTo(other.coordinate, highAccuracy)
    }

    fun verticalDistanceTo(other: Location): Float {
        return elevation.meters().distance - other.elevation.meters().distance
    }

    fun bearingTo(other: Location, highAccuracy: Boolean = true): Bearing {
        return coordinate.bearingTo(other.coordinate, highAccuracy)
    }
}