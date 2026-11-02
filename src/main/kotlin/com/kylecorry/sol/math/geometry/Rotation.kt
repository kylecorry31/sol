package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector3Precise
import com.kylecorry.sol.math.algebra.SymmetricEigen
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class Rotation private constructor(private val values: DoubleArray) {

    fun rotate(vector: Vector3Precise): Vector3Precise {
        return Vector3Precise(
            values[0] * vector.x + values[1] * vector.y + values[2] * vector.z,
            values[3] * vector.x + values[4] * vector.y + values[5] * vector.z,
            values[6] * vector.x + values[7] * vector.y + values[8] * vector.z
        )
    }

    fun inverse(): Rotation {
        return Rotation(
            doubleArrayOf(
                values[0], values[3], values[6],
                values[1], values[4], values[7],
                values[2], values[5], values[8]
            )
        )
    }

    operator fun times(other: Rotation): Rotation {
        val out = DoubleArray(9)
        for (i in 0..2) {
            for (j in 0..2) {
                var sum = 0.0
                for (k in 0..2) {
                    sum += values[i * 3 + k] * other.values[k * 3 + j]
                }
                out[i * 3 + j] = sum
            }
        }
        return Rotation(out)
    }

    /**
     * @return the row major matrix
     */
    fun toDoubleArray(): DoubleArray {
        return values.copyOf()
    }

    /**
     * The angle of this rotation in degrees
     */
    fun angle(): Double {
        val trace = values[0] + values[4] + values[8]
        return Math.toDegrees(acos(((trace - 1) / 2).coerceIn(-1.0, 1.0)))
    }

    override fun equals(other: Any?): Boolean {
        return other is Rotation && values.contentEquals(other.values)
    }

    override fun hashCode(): Int {
        return values.contentHashCode()
    }

    companion object {
        val identity = Rotation(doubleArrayOf(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0))

        /**
         * @param values a row major 3x3 rotation matrix
         */
        fun fromMatrix(values: DoubleArray): Rotation {
            require(values.size == 9) { "A rotation matrix must have 9 values" }
            return Rotation(values.copyOf())
        }

        /**
         * A right handed rotation about an axis
         */
        fun fromAxisAngle(axis: Vector3Precise, degrees: Double): Rotation {
            val n = axis.normalize()
            val t = Math.toRadians(degrees)
            val c = cos(t)
            val s = sin(t)
            val k = 1 - c
            return Rotation(
                doubleArrayOf(
                    c + n.x * n.x * k, n.x * n.y * k - n.z * s, n.x * n.z * k + n.y * s,
                    n.y * n.x * k + n.z * s, c + n.y * n.y * k, n.y * n.z * k - n.x * s,
                    n.z * n.x * k - n.y * s, n.z * n.y * k + n.x * s, c + n.z * n.z * k
                )
            )
        }

        fun fromQuaternion(w: Double, x: Double, y: Double, z: Double): Rotation {
            return Rotation(
                doubleArrayOf(
                    1 - 2 * (y * y + z * z), 2 * (x * y - z * w), 2 * (x * z + y * w),
                    2 * (x * y + z * w), 1 - 2 * (x * x + z * z), 2 * (y * z - x * w),
                    2 * (x * z - y * w), 2 * (y * z + x * w), 1 - 2 * (x * x + y * y)
                )
            )
        }

        /**
         * TRIAD: the rotation that maps from1 exactly onto to1 and from2 as close as possible to to2.
         * @return the rotation or null if either pair of vectors is parallel
         */
        fun triad(
            from1: Vector3Precise,
            from2: Vector3Precise,
            to1: Vector3Precise,
            to2: Vector3Precise
        ): Rotation? {
            val fromCross = from1.cross(from2)
            val toCross = to1.cross(to2)
            if (fromCross.magnitude() < PARALLEL_THRESHOLD || toCross.magnitude() < PARALLEL_THRESHOLD) {
                return null
            }
            val t1 = from1.normalize()
            val t2 = fromCross.normalize()
            val t3 = t1.cross(t2)
            val s1 = to1.normalize()
            val s2 = toCross.normalize()
            val s3 = s1.cross(s2)
            val t = listOf(t1.toDoubleArray(), t2.toDoubleArray(), t3.toDoubleArray())
            val s = listOf(s1.toDoubleArray(), s2.toDoubleArray(), s3.toDoubleArray())
            val out = DoubleArray(9)
            for (i in 0..2) {
                for (j in 0..2) {
                    out[i * 3 + j] = s[0][i] * t[0][j] + s[1][i] * t[1][j] + s[2][i] * t[2][j]
                }
            }
            return Rotation(out)
        }

        /**
         * The least squares rotation that maps each from vector onto its to vector (Wahba's problem),
         * solved with Horn's quaternion method.
         */
        fun fit(
            from: List<Vector3Precise>,
            to: List<Vector3Precise>,
            weights: List<Double>? = null
        ): Rotation {
            require(from.size == to.size) { "The vector lists must be the same size" }
            val s = Array(3) { DoubleArray(3) }
            for (i in from.indices) {
                val w = weights?.get(i) ?: 1.0
                val a = from[i].toDoubleArray()
                val b = to[i].toDoubleArray()
                for (r in 0..2) {
                    for (c in 0..2) {
                        s[r][c] += w * a[r] * b[c]
                    }
                }
            }
            val n = arrayOf(
                doubleArrayOf(s[0][0] + s[1][1] + s[2][2], s[1][2] - s[2][1], s[2][0] - s[0][2], s[0][1] - s[1][0]),
                doubleArrayOf(s[1][2] - s[2][1], s[0][0] - s[1][1] - s[2][2], s[0][1] + s[1][0], s[2][0] + s[0][2]),
                doubleArrayOf(s[2][0] - s[0][2], s[0][1] + s[1][0], -s[0][0] + s[1][1] - s[2][2], s[1][2] + s[2][1]),
                doubleArrayOf(s[0][1] - s[1][0], s[2][0] + s[0][2], s[1][2] + s[2][1], -s[0][0] - s[1][1] + s[2][2])
            )
            val q = SymmetricEigen.largestEigenvector(n)
            return fromQuaternion(q[0], q[1], q[2], q[3])
        }

        private const val PARALLEL_THRESHOLD = 1e-9
    }
}
