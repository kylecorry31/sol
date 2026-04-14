package com.kylecorry.sol.units

import com.kylecorry.sol.math.MathExtensions.toDegrees
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.trigonometry.Trigonometry.cosDegrees
import com.kylecorry.sol.math.trigonometry.Trigonometry.sinDegrees
import com.kylecorry.sol.science.geography.Geography
import com.kylecorry.sol.science.geography.formatting.*
import kotlin.math.*

/**
 * A geographic coordinate with precision down to about 1cm (7 decimal places). Latitude is clamped to the range [-90, 90] and longitude is wrapped to the range [-180, 180].
 * If unclamped or more precise values are needeed, use the [DoubleCoordinate] class instead.
 */
@JvmInline
value class Coordinate internal constructor(val packed: Long) : ICoordinate {

    constructor(latitude: Double, longitude: Double) : this(
        pack(latitude.coerceIn(-90.0, 90.0), toLongitude(longitude))
    )

    override val latitude: Double
        get() = unpackLatitude(packed)

    override val longitude: Double
        get() = unpackLongitude(packed)

    val isNorthernHemisphere: Boolean
        get() = latitude >= 0

    val antipode: Coordinate
        get() = Coordinate(-latitude, toLongitude(longitude + 180))

    override fun toString(): String {
        return "$latitude, $longitude"
    }

    fun copy(latitude: Double = this.latitude, longitude: Double = this.longitude): Coordinate {
        return Coordinate(latitude, longitude)
    }

    operator fun component1(): Double {
        return latitude
    }

    operator fun component2(): Double {
        return longitude
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
        private const val LONGITUDE_BITS = 32
        private const val LONGITUDE_MASK = 0xFFFFFFFFL
        private const val COORDINATE_SCALE = 1e7

        fun toLongitude(degrees: Double): Double {
            return Arithmetic.wrap(degrees, -180.0, 180.0)
        }

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

        internal fun pack(latitude: Double, longitude: Double): Long {
            val lat = (latitude * COORDINATE_SCALE).roundToLong().toInt()
            val lon = (longitude * COORDINATE_SCALE).roundToLong().toInt()
            return (lat.toLong() shl LONGITUDE_BITS) or (lon.toLong() and LONGITUDE_MASK)
        }

        internal fun unpackLatitude(packed: Long): Double {
            return (packed shr LONGITUDE_BITS).toInt() / COORDINATE_SCALE
        }

        internal fun unpackLongitude(packed: Long): Double {
            return (packed and LONGITUDE_MASK).toInt() / COORDINATE_SCALE
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

    }
}
