package gov.nasa.worldwind.geom

import kotlin.math.PI

/**
 * Represents a geometric angle. Instances of `Angle` are immutable. An angle can be obtained through the
 * factory methods [.fromDegrees] and [.fromRadians].
 *
 * @author Tom Gaskins
 * @version $Id$
 */
class Angle : Comparable<Angle?> {
    /**
     * Retrieves the size of this angle in degrees. This method may be faster than first obtaining the radians and then
     * converting to degrees.
     *
     * @return the size of this angle in degrees.
     */
    val degrees: Double

    /**
     * Retrieves the size of this angle in radians. This may be useful for `java.lang.Math` functions, which
     * generally take radians as trigonometric arguments. This method may be faster that first obtaining the degrees and
     * then converting to radians.
     *
     * @return the size of this angle in radians.
     */
    val radians: Double

    constructor(angle: Angle) {
        this.degrees = angle.degrees
        this.radians = angle.radians
    }

    private constructor(degrees: Double, radians: Double) {
        this.degrees = degrees
        this.radians = radians
    }

    /**
     * Obtains the sum of these two angles. Does not accept a null argument. This method is commutative, so
     * `a.add(b)` and `b.add(a)` are equivalent. Neither this angle nor angle is changed, instead
     * the result is returned as a new angle.
     *
     * @param angle the angle to add to this one.
     *
     * @return an angle whose size is the total of this angles and angles size.
     *
     * @throws IllegalArgumentException if angle is null.
     */
    fun add(angle: Angle): Angle {

        return fromDegrees(this.degrees + angle.degrees)
    }

    /**
     * Obtains the difference of these two angles. Does not accept a null argument. This method is not commutative.
     * Neither this angle nor angle is changed, instead the result is returned as a new angle.
     *
     * @param angle the angle to subtract from this angle.
     *
     * @return a new angle correpsonding to this angle's size minus angle's size.
     *
     * @throws IllegalArgumentException if angle is null.
     */
    fun subtract(angle: Angle): Angle {

        return fromDegrees(this.degrees - angle.degrees)
    }

    /**
     * Obtains the sine of this angle.
     *
     * @return the trigonometric sine of this angle.
     */
    fun sin(): Double {
        return kotlin.math.sin(this.radians)
    }

    /**
     * Obtains the cosine of this angle.
     *
     * @return the trigonometric cosine of this angle.
     */
    fun cos(): Double {
        return kotlin.math.cos(this.radians)
    }

    /**
     * Compares this [Angle] with another. Returns a negative integer if this is the smaller angle, a positive
     * integer if this is the larger, and zero if both angles are equal.
     *
     * @param other the angle to compare against.
     *
     * @return -1 if this angle is smaller, 0 if both are equal and +1 if this angle is larger.
     *
     * @throws IllegalArgumentException if angle is null.
     */
    override fun compareTo(other: Angle?): Int {
        requireNotNull(other) { "Angle Is Null" }

        if (this.degrees < other.degrees) return -1

        if (this.degrees > other.degrees) return 1

        return 0
    }

    /**
     * Obtains a `String` representation of this angle.
     *
     * @return the value of this angle in degrees and as a `String`.
     */
    override fun toString(): String {
        return this.degrees.toString() + '\u00B0'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val angle = other as Angle

        return angle.degrees == this.degrees
    }

    override fun hashCode(): Int {
        val temp = if (degrees != 0.0) degrees.toBits() else 0L
        return (temp xor (temp ushr 32)).toInt()
    }

    companion object {

        /** Represents an angle of zero degrees  */
        val ZERO: Angle = fromDegrees(0.0)

        private const val DEGREES_TO_RADIANS = PI / 180.0
        private const val RADIANS_TO_DEGREES = 180.0 / PI

        /**
         * Obtains an angle from a specified number of degrees.
         *
         * @param degrees the size in degrees of the angle to be obtained
         *
         * @return a new angle, whose size in degrees is given by `degrees`
         */
        fun fromDegrees(degrees: Double): Angle {
            return Angle(degrees, DEGREES_TO_RADIANS * degrees)
        }

        /**
         * Obtains an angle from a specified number of radians.
         *
         * @param radians the size in radians of the angle to be obtained.
         *
         * @return a new angle, whose size in radians is given by `radians`.
         */
        fun fromRadians(radians: Double): Angle {
            return Angle(RADIANS_TO_DEGREES * radians, radians)
        }

        private const val HALF_PI = Math.PI / 2

        fun fromDegreesLatitude(degrees: Double): Angle {
            var degrees = degrees
            degrees = if (degrees < -90) -90.0 else if (degrees > 90) 90.0 else degrees
            var radians: Double = DEGREES_TO_RADIANS * degrees
            radians = if (radians < -HALF_PI) -HALF_PI else if (radians > HALF_PI) HALF_PI else radians

            return Angle(degrees, radians)
        }

        fun fromDegreesLongitude(degrees: Double): Angle {
            var degrees = degrees
            degrees = if (degrees < -180) -180.0 else if (degrees > 180) 180.0 else degrees
            var radians: Double = DEGREES_TO_RADIANS * degrees
            radians = if (radians < -Math.PI) -Math.PI else if (radians > Math.PI) Math.PI else radians

            return Angle(degrees, radians)
        }

        fun asin(sine: Double): Angle {
            return fromRadians(kotlin.math.asin(sine))
        }

        fun acos(cosine: Double): Angle {   //Tom: this method is not used, should we delete it? (13th Dec 06)
            return fromRadians(kotlin.math.acos(cosine))
        }

        fun atan(tan: Double): Angle {   //Tom: this method is not used, should we delete it? (13th Dec 06)
            return fromRadians(kotlin.math.atan(tan))
        }

        /**
         * Obtains the average of three angles. The order of parameters does not matter.
         *
         * @param a the first angle.
         * @param b the second angle.
         *
         * @return the average of `a1`, `a2` and `a3`
         *
         * @throws IllegalArgumentException if `a` or `b` is null
         */
        fun average(a: Angle, b: Angle): Angle {
            return fromDegrees(0.5 * (a.degrees + b.degrees))
        }

        /**
         * Obtains the average of three angles. The order of parameters does not matter.
         *
         * @param a the first angle.
         * @param b the second angle.
         * @param c the third angle.
         *
         * @return the average of `a1`, `a2` and `a3`.
         *
         * @throws IllegalArgumentException if `a`, `b` or `c` is null.
         */
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

        fun normalizedLatitude(unnormalizedAngle: Angle): Angle {
            return fromDegrees(normalizedDegreesLatitude(unnormalizedAngle.degrees))
        }

        fun normalizedLongitude(unnormalizedAngle: Angle): Angle {
            return fromDegrees(normalizedDegreesLongitude(unnormalizedAngle.degrees))
        }

        fun max(a: Angle, b: Angle): Angle {
            return if (a.degrees >= b.degrees) a else b
        }

        fun min(a: Angle, b: Angle): Angle {
            return if (a.degrees <= b.degrees) a else b
        }
    }
}