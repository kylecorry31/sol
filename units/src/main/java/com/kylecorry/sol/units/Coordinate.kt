package com.kylecorry.sol.units

import android.os.Parcelable
import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlin.math.*


@Parcelize
data class Coordinate(val latitude: Double, val longitude: Double) : Parcelable {

    @IgnoredOnParcel
    val isNorthernHemisphere = latitude > 0

    override fun toString(): String {
        return "$latitude, $longitude"
    }

    fun distanceTo(other: Coordinate): Float {
        return DistanceCalculator.getDistanceAndBearing(this, other)[0]
    }

    fun plus(distance: Distance, bearing: Bearing): Coordinate {
        return plus(distance.meters().distance.toDouble(), bearing)
    }

    fun plus(meters: Double, bearing: Bearing): Coordinate {
        // Adapted from https://www.movable-type.co.uk/scripts/latlong.html
        val radius = 6371.2e3
        val newLat = asin(
            sinDegrees(latitude) * cos(meters / radius) +
                    cosDegrees(latitude) * sin(meters / radius) * cosDegrees(bearing.value.toDouble())
        ).toDegrees()

        val newLng = longitude + atan2(
            sinDegrees(bearing.value.toDouble()) * sin(meters / radius) * cosDegrees(latitude),
            cos(meters / radius) - sinDegrees(latitude) * sinDegrees(newLat)
        ).toDegrees()

        val normalLng = (newLng + 540) % 360 - 180

        return Coordinate(newLat, normalLng)
    }

    /**
     * Get the bearing to the other coordinate (using True North)
     */
    fun bearingTo(other: Coordinate): Bearing {
        return Bearing(DistanceCalculator.getDistanceAndBearing(this, other)[1])
    }

    companion object {

        val zero = Coordinate(0.0, 0.0)

    }
}