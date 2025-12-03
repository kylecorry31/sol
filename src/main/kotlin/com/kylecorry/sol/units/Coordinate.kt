package com.kylecorry.sol.units

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.science.geography.formatting.*
import com.kylecorry.sol.shared.FixedPoint32
import kotlin.math.*


@JvmInline
value class Coordinate internal constructor(internal val bits: Long) {
    constructor (latitude: Double, longitude: Double) : this(
        (
                (FixedPoint32(
                    latitude.coerceIn(-90.0, 90.0),
                    7
                ).bits.toLong() shl 32) or (FixedPoint32(toLongitude(longitude), 7).bits.toLong() and 0xFFFFFFFFL)
        )
    )

    val latitude: Double
        get() = FixedPoint32((bits ushr 32).toInt()).toDouble(7)

    val longitude: Double
        get() = FixedPoint32((bits and 0xFFFFFFFFL).toInt()).toDouble(7)

    val isNorthernHemisphere: Boolean
        get() = latitude >= 0

    val antipode: Coordinate
        get() = Coordinate(-latitude, toLongitude(longitude + 180))

    override fun toString(): String {
        return "$latitude, $longitude"
    }

    fun distanceTo(other: Coordinate, highAccuracy: Boolean = true): Float {
        return if (highAccuracy) {
            DistanceCalculator.vincenty(this, other)[0]
        } else {
            DistanceCalculator.haversine(this, other, EARTH_AVERAGE_RADIUS)[0]
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
            Bearing.from(DistanceCalculator.vincenty(this, other)[1])
        } else {
            Bearing.from(DistanceCalculator.haversine(this, other, EARTH_AVERAGE_RADIUS)[1])
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
            return SolMath.wrap(degrees, -180.0, 180.0)
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