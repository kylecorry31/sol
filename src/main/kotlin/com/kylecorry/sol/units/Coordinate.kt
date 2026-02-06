package com.kylecorry.sol.units
import com.kylecorry.sol.math.arithmetic.Arithmetic

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.analysis.Trigonometry.cosDegrees
import com.kylecorry.sol.math.analysis.Trigonometry.sinDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.science.geography.Geography
import com.kylecorry.sol.science.geography.formatting.*
import kotlin.math.*


data class Coordinate(val latitude: Double, val longitude: Double) {

    val isNorthernHemisphere = latitude >= 0

    val antipode: Coordinate
        get() = Coordinate(-latitude, toLongitude(longitude + 180))

    override fun toString(): String {
        return "$latitude, $longitude"
    }

    fun distanceTo(other: Coordinate, highAccuracy: Boolean = true): Float {
        return if (highAccuracy) {
            Geography.vincenty(this, other)[0]
        } else {
            Geography.haversine(this, other, EARTH_AVERAGE_RADIUS)[0]
        }
    }

    fun plus(distance: Distance, bearing: Bearing): Coordinate {
        return plus(distance.meters().value.toDouble(), bearing)
    }

    fun plus(meters: Double, bearing: Bearing): Coordinate {
        // Adapted from https://www.movable-type.co.uk/scripts/latlong.html
        val radius = EARTH_AVERAGE_RADIUS
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
    fun bearingTo(other: Coordinate, highAccuracy: Boolean = true): Bearing {
        return if (highAccuracy) {
            Bearing.from(Geography.vincenty(this, other)[1])
        } else {
            Bearing.from(Geography.haversine(this, other, EARTH_AVERAGE_RADIUS)[1])
        }
    }

    fun toCartesian(): Vector3 {
        val x = cosDegrees(latitude) * cosDegrees(longitude)
        val y = cosDegrees(latitude) * sinDegrees(longitude)
        val z = sinDegrees(latitude)

        return Vector3(x.toFloat(), y.toFloat(), z.toFloat())
    }

    /**
     * Get the angle between two coordinates (through the center of the Earth)
     */
    fun degreesBetween(other: Coordinate): Float {
        return acos(
            sinDegrees(latitude) * sinDegrees(other.latitude) +
                    cosDegrees(latitude) * cosDegrees(other.latitude) *
                    cosDegrees(longitude - other.longitude)
        ).toDegrees().toFloat().absoluteValue
    }

    companion object {

        val zero = Coordinate(0.0, 0.0)
        private const val EARTH_AVERAGE_RADIUS = 6371.2e3

        fun toLongitude(degrees: Double): Double {
            return Arithmetic.wrap(degrees, -180.0, 180.0)
        }

        fun constrained(latitude: Double, longitude: Double): Coordinate {
            return Coordinate(latitude.coerceIn(-90.0, 90.0), toLongitude(longitude))
        }

        private val defaultFormats = listOf(
            DecimalDegreesCoordinateFormat(),
            DegreesDecimalMinutesCoordinateFormat(),
            DegreesMinutesSecondsCoordinateFormat(),
            UTMCoordinateFormat(),
            MGRSCoordinateFormat(),
            USNGCoordinateFormat(),
            OSGBCoordinateFormat()
        )

        fun parse(
            location: String,
            formats: List<CoordinateFormat> = defaultFormats
        ): Coordinate? {
            return formats.firstNotNullOfOrNull { it.parse(location) }
        }

        internal fun isValidLongitude(longitude: Double): Boolean {
            return longitude.absoluteValue <= 180
        }

        internal fun isValidLatitude(latitude: Double): Boolean {
            return latitude.absoluteValue <= 90
        }

    }
}