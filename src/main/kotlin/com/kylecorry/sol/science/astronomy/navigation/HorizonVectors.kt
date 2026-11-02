package com.kylecorry.sol.science.astronomy.navigation

import com.kylecorry.sol.math.Vector3Precise
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Conversions between horizon coordinates and East-North-Up unit vectors
 */
internal object HorizonVectors {

    val up = Vector3Precise(0.0, 0.0, 1.0)

    /**
     * @param azimuth degrees clockwise from north
     * @param altitude degrees above the horizon
     */
    fun toVector(azimuth: Double, altitude: Double): Vector3Precise {
        val az = Math.toRadians(azimuth)
        val alt = Math.toRadians(altitude)
        return Vector3Precise(cos(alt) * sin(az), cos(alt) * cos(az), sin(alt))
    }

    /**
     * @return the azimuth (0 to 360 degrees) and altitude (degrees)
     */
    fun fromVector(vector: Vector3Precise): Pair<Double, Double> {
        val n = vector.normalize()
        val altitude = Math.toDegrees(asin(n.z.coerceIn(-1.0, 1.0)))
        val azimuth = (Math.toDegrees(atan2(n.x, n.y)) + 360) % 360
        return azimuth to altitude
    }
}
