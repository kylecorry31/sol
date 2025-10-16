/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom

import kotlin.math.*

/**
 * Represents a geometric angle. Instances of [Angle] are immutable. An angle can be obtained through the
 * factory methods [fromDegrees] and [fromRadians].
 *
 * @author Tom Gaskins
 * @version $Id$
 */
class Angle private constructor(
    @JvmField val degrees: Double,
    @JvmField val radians: Double
) : Comparable<Angle> {

    constructor(angle: Angle) : this(angle.degrees, angle.radians)

    /**
     * Retrieves the size of this angle in degrees. This method may be faster than first obtaining the radians and then
     * converting to degrees.
     *
     * @return the size of this angle in degrees.
     */
    fun getDegrees(): Double = degrees

    /**
     * Retrieves the size of this angle in radians. This may be useful for `java.lang.Math` functions, which
     * generally take radians as trigonometric arguments. This method may be faster that first obtaining the degrees and
     * then converting to radians.
     *
     * @return the size of this angle in radians.
     */
    fun getRadians(): Double = radians

    fun add(angle: Angle): Angle {
        return fromDegrees(degrees + angle.degrees)
    }

    fun subtract(angle: Angle): Angle {
        return fromDegrees(degrees - angle.degrees)
    }

    fun multiply(multiplier: Double): Angle {
        return fromDegrees(degrees * multiplier)
    }

    fun divide(angle: Angle): Double {
        require(angle.degrees != 0.0) { "Divide By Zero" }
        return degrees / angle.degrees
    }

    fun addDegrees(degrees: Double): Angle {
        return fromDegrees(this.degrees + degrees)
    }

    fun subtractDegrees(degrees: Double): Angle {
        return fromDegrees(this.degrees - degrees)
    }

    fun divide(divisor: Double): Angle {
        return fromDegrees(degrees / divisor)
    }

    fun addRadians(radians: Double): Angle {
        return fromRadians(this.radians + radians)
    }

    fun subtractRadians(radians: Double): Angle {
        return fromRadians(this.radians - radians)
    }

    fun angularDistanceTo(angle: Angle): Angle {
        var differenceDegrees = angle.subtract(this).degrees
        if (differenceDegrees < -180)
            differenceDegrees += 360
        else if (differenceDegrees > 180)
            differenceDegrees -= 360

        val absAngle = abs(differenceDegrees)
        return fromDegrees(absAngle)
    }

    fun sin(): Double = sin(radians)

    fun sinHalfAngle(): Double = sin(0.5 * radians)

    fun cos(): Double = cos(radians)

    fun cosHalfAngle(): Double = cos(0.5 * radians)

    fun tanHalfAngle(): Double = tan(0.5 * radians)

    fun normalizedLatitude(): Angle = normalizedLatitude(this)

    fun normalizedLongitude(): Angle = normalizedLongitude(this)

    override fun compareTo(other: Angle): Int {
        return when {
            degrees < other.degrees -> -1
            degrees > other.degrees -> 1
            else -> 0
        }
    }

    override fun toString(): String = "$degrees\u00B0"

    fun toDecimalDegreesString(digits: Int): String {
        require(digits in 0..15) { "Argument Out Of Range" }
        return String.format("%.${digits}f\u00B0", degrees)
    }

    fun toDMSString(): String {
        var temp = degrees
        val sign = sign(temp).toInt()
        temp *= sign
        var d = floor(temp).toInt()
        temp = (temp - d) * 60.0
        var m = floor(temp).toInt()
        temp = (temp - m) * 60.0
        var s = round(temp).toInt()

        if (s == 60) {
            m++
            s = 0
        }
        if (m == 60) {
            d++
            m = 0
        }

        return "${if (sign == -1) "-" else ""}$d\u00B0 $m\u2019 $s\u201d"
    }

    fun toFormattedDMSString(): String {
        var temp = degrees
        val sign = sign(temp).toInt()

        temp *= sign
        var d = floor(temp).toInt()
        temp = (temp - d) * 60.0
        var m = floor(temp).toInt()
        temp = (temp - m) * 60.0
        var s = (round(temp * 100) / 100)

        if (s == 60.0) {
            m++
            s = 0.0
        }
        if (m == 60) {
            d++
            m = 0
        }

        return String.format("%4d\u00B0 %2d\u2019 %5.2f\u201d", sign * d, m, s)
    }

    fun toDMS(): DoubleArray {
        var temp = degrees
        val sign = sign(temp).toInt()

        temp *= sign
        var d = floor(temp).toInt()
        temp = (temp - d) * 60.0
        var m = floor(temp).toInt()
        temp = (temp - m) * 60.0
        var s = (round(temp * 100) / 100)

        if (s == 60.0) {
            m++
            s = 0.0
        }
        if (m == 60) {
            d++
            m = 0
        }

        return doubleArrayOf((sign * d).toDouble(), m.toDouble(), s)
    }

    fun getSizeInBytes(): Long = java.lang.Double.SIZE / 8L

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val angle = other as Angle
        return angle.degrees == degrees
    }

    override fun hashCode(): Int {
        val temp = if (degrees != +0.0) degrees.toBits() else 0L
        return (temp xor (temp ushr 32)).toInt()
    }

    companion object {
        // Angle format
        const val ANGLE_FORMAT_DD = "gov.nasa.worldwind.Geom.AngleDD"
        const val ANGLE_FORMAT_DMS = "gov.nasa.worldwind.Geom.AngleDMS"

        /** Represents an angle of zero degrees */
        @JvmField
        val ZERO = fromDegrees(0.0)

        /** Represents a right angle of positive 90 degrees */
        @JvmField
        val POS90 = fromDegrees(90.0)

        /** Represents a right angle of negative 90 degrees */
        @JvmField
        val NEG90 = fromDegrees(-90.0)

        /** Represents an angle of positive 180 degrees */
        @JvmField
        val POS180 = fromDegrees(180.0)

        /** Represents an angle of negative 180 degrees */
        @JvmField
        val NEG180 = fromDegrees(-180.0)

        /** Represents an angle of positive 360 degrees */
        @JvmField
        val POS360 = fromDegrees(360.0)

        /** Represents an angle of negative 360 degrees */
        @JvmField
        val NEG360 = fromDegrees(-360.0)

        /** Represents an angle of 1 minute */
        @JvmField
        val MINUTE = fromDegrees(1.0 / 60.0)

        /** Represents an angle of 1 second */
        @JvmField
        val SECOND = fromDegrees(1.0 / 3600.0)

        private const val DEGREES_TO_RADIANS = PI / 180.0
        private const val RADIANS_TO_DEGREES = 180.0 / PI
        private const val PIOver2 = PI / 2

        @JvmStatic
        fun fromDegrees(degrees: Double): Angle {
            return Angle(degrees, DEGREES_TO_RADIANS * degrees)
        }

        @JvmStatic
        fun fromRadians(radians: Double): Angle {
            return Angle(RADIANS_TO_DEGREES * radians, radians)
        }

        @JvmStatic
        fun fromDegreesLatitude(degrees: Double): Angle {
            val clampedDegrees = degrees.coerceIn(-90.0, 90.0)
            val radians = (DEGREES_TO_RADIANS * clampedDegrees).coerceIn(-PIOver2, PIOver2)
            return Angle(clampedDegrees, radians)
        }

        @JvmStatic
        fun fromRadiansLatitude(radians: Double): Angle {
            val clampedRadians = radians.coerceIn(-PIOver2, PIOver2)
            val degrees = (RADIANS_TO_DEGREES * clampedRadians).coerceIn(-90.0, 90.0)
            return Angle(degrees, clampedRadians)
        }

        @JvmStatic
        fun fromDegreesLongitude(degrees: Double): Angle {
            val clampedDegrees = degrees.coerceIn(-180.0, 180.0)
            val radians = (DEGREES_TO_RADIANS * clampedDegrees).coerceIn(-PI, PI)
            return Angle(clampedDegrees, radians)
        }

        @JvmStatic
        fun fromRadiansLongitude(radians: Double): Angle {
            val clampedRadians = radians.coerceIn(-PI, PI)
            val degrees = (RADIANS_TO_DEGREES * clampedRadians).coerceIn(-180.0, 180.0)
            return Angle(degrees, clampedRadians)
        }

        @JvmStatic
        fun fromXY(x: Double, y: Double): Angle {
            val radians = atan2(y, x)
            return Angle(RADIANS_TO_DEGREES * radians, radians)
        }

        @JvmStatic
        fun fromDMS(degrees: Int, minutes: Int, seconds: Int): Angle {
            require(minutes in 0 until 60) { "Argument Out Of Range" }
            require(seconds in 0 until 60) { "Argument Out Of Range" }
            return fromDegrees(sign(degrees.toDouble()) * (abs(degrees) + minutes / 60.0 + seconds / 3600.0))
        }

        @JvmStatic
        fun fromDMdS(degrees: Int, minutes: Double): Angle {
            require(minutes >= 0.0 && minutes < 60.0) { "Argument Out Of Range" }
            return fromDegrees(sign(degrees.toDouble()) * (abs(degrees) + minutes / 60.0))
        }

        @JvmStatic
        fun fromDMS(dmsString: String): Angle {
            // Check for string format validity
            val regex = """([-+]?\d{1,3}[dD°\s](\s*\d{1,2}['\u2019\s])?(\s*\d{1,2}["\u201d\s])?\s*([NnSsEeWw])?\s?)""".toRegex()
            val matcher = regex.find("$dmsString ")
                ?: throw IllegalArgumentException("Argument Out Of Range")

            // Replace degree, min and sec signs with space
            var cleanedDms = dmsString.replace(Regex("[Dd°'\u2019\"\u201d]"), " ")
            // Replace multiple spaces with single ones
            cleanedDms = cleanedDms.replace(Regex("\\s+"), " ")
            cleanedDms = cleanedDms.trim()

            // Check for sign prefix and suffix
            var sign = 1
            val suffix = cleanedDms.uppercase().last()
            if (!suffix.isDigit()) {
                sign = if (suffix == 'S' || suffix == 'W') -1 else 1
                cleanedDms = cleanedDms.dropLast(1).trim()
            }
            val prefix = cleanedDms.first()
            if (!prefix.isDigit()) {
                sign *= if (prefix == '-') -1 else 1
                cleanedDms = cleanedDms.drop(1)
            }

            // Extract degrees, minutes and seconds
            val dms = cleanedDms.split(" ")
            val d = dms[0].toInt()
            val m = if (dms.size > 1) dms[1].toInt() else 0
            val s = if (dms.size > 2) dms[2].toInt() else 0

            return fromDMS(d, m, s).multiply(sign.toDouble())
        }

        @JvmStatic
        fun asin(sine: Double): Angle = fromRadians(kotlin.math.asin(sine))

        @JvmStatic
        fun acos(cosine: Double): Angle = fromRadians(kotlin.math.acos(cosine))

        @JvmStatic
        fun atan(tan: Double): Angle = fromRadians(kotlin.math.atan(tan))

        @JvmStatic
        fun midAngle(a1: Angle, a2: Angle): Angle {
            return fromDegrees(0.5 * (a1.degrees + a2.degrees))
        }

        @JvmStatic
        fun average(a: Angle, b: Angle): Angle {
            return fromDegrees(0.5 * (a.degrees + b.degrees))
        }

        @JvmStatic
        fun average(a: Angle, b: Angle, c: Angle): Angle {
            return fromDegrees((a.degrees + b.degrees + c.degrees) / 3)
        }

        private fun normalizedDegreesLatitude(degrees: Double): Double {
            val lat = degrees % 180
            return if (lat > 90) 180 - lat else if (lat < -90) -180 - lat else lat
        }

        private fun normalizedDegreesLongitude(degrees: Double): Double {
            val lon = degrees % 360
            return if (lon > 180) lon - 360 else if (lon < -180) 360 + lon else lon
        }

        @JvmStatic
        fun normalizedLatitude(unnormalizedAngle: Angle): Angle {
            return fromDegrees(normalizedDegreesLatitude(unnormalizedAngle.degrees))
        }

        @JvmStatic
        fun normalizedLongitude(unnormalizedAngle: Angle): Angle {
            return fromDegrees(normalizedDegreesLongitude(unnormalizedAngle.degrees))
        }

        @JvmStatic
        fun crossesLongitudeBoundary(angleA: Angle, angleB: Angle): Boolean {
            // A segment cross the line if end pos have different longitude signs
            // and are more than 180 degrees longitude apart
            return (sign(angleA.degrees) != sign(angleB.degrees)) &&
                    (abs(angleA.degrees - angleB.degrees) > 180)
        }

        @JvmStatic
        fun isValidLatitude(value: Double): Boolean = value in -90.0..90.0

        @JvmStatic
        fun isValidLongitude(value: Double): Boolean = value in -180.0..180.0

        @JvmStatic
        fun max(a: Angle, b: Angle): Angle = if (a.degrees >= b.degrees) a else b

        @JvmStatic
        fun min(a: Angle, b: Angle): Angle = if (a.degrees <= b.degrees) a else b
    }
}
