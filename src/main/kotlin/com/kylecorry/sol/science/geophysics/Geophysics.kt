package com.kylecorry.sol.science.geophysics

import com.kylecorry.sol.math.analysis.Trigonometry.sinDegrees
import com.kylecorry.sol.math.arithmetic.Arithmetic.square
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.science.geology.ReferenceEllipsoid
import com.kylecorry.sol.units.*
import java.time.Instant
import kotlin.math.*

object Geophysics {

    const val GRAVITY = 9.81f

    private val worldMagneticModel = SphericalHarmonics(
        WorldMagneticModel2025.G_COEFFICIENTS,
        WorldMagneticModel2025.H_COEFFICIENTS,
        baseTime = WorldMagneticModel2025.BASE_TIME,
        deltaGCoefficients = WorldMagneticModel2025.DELTA_G,
        deltaHCoefficients = WorldMagneticModel2025.DELTA_H
    )

    fun getGeomagneticDeclination(
        coordinate: Coordinate,
        altitude: Float? = null,
        time: Long = System.currentTimeMillis()
    ): Float {
        val geoField = worldMagneticModel.getVector(
            coordinate,
            Distance.meters(altitude ?: 0f),
            Instant.ofEpochMilli(time)
        )
        return atan2(geoField.y, geoField.x).toDegrees()
    }

    fun getGeomagneticInclination(
        coordinate: Coordinate,
        altitude: Float? = null,
        time: Long = System.currentTimeMillis()
    ): Float {
        val geoField = worldMagneticModel.getVector(
            coordinate,
            Distance.meters(altitude ?: 0f),
            Instant.ofEpochMilli(time)
        )
        return atan2(geoField.z, hypot(geoField.x, geoField.y)).toDegrees()
    }

    fun getGeomagneticField(
        coordinate: Coordinate,
        altitude: Float? = null,
        time: Long = System.currentTimeMillis()
    ): Vector3 {
        val geoField = worldMagneticModel.getVector(
            coordinate,
            Distance.meters(altitude ?: 0f),
            Instant.ofEpochMilli(time)
        )
        return Vector3(geoField.x * 0.001f, geoField.y * 0.001f, geoField.z * 0.001f)
    }

    fun getGravity(coordinate: Coordinate): Float {
        // Somigliana equation (IGF80)
        val ellipsoid = ReferenceEllipsoid.wgs84
        val ge = 9.78032677153489
        val k = 0.001931851353260676
        val e2 = ellipsoid.squaredEccentricity
        val sinLat2 = square(sinDegrees(coordinate.latitude))
        return (ge * (1 + k * sinLat2) / sqrt(1 - e2 * sinLat2)).toFloat()
    }

    fun getAzimuth(gravity: Vector3, magneticField: Vector3): Bearing {
        return AzimuthCalculator.calculate(gravity, magneticField) ?: Bearing.from(0f)
    }
}
