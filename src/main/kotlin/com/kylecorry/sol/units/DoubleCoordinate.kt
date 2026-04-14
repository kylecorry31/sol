package com.kylecorry.sol.units

import com.kylecorry.sol.math.MathExtensions.toDegrees
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.math.trigonometry.Trigonometry.cosDegrees
import com.kylecorry.sol.math.trigonometry.Trigonometry.sinDegrees
import com.kylecorry.sol.science.geography.Geography
import com.kylecorry.sol.science.geography.formatting.*
import kotlin.math.*

data class DoubleCoordinate(
    override val latitude: Double,
    override val longitude: Double
) : ICoordinate {

    override fun toString(): String {
        return "$latitude, $longitude"
    }

    fun toCoordinate(): Coordinate {
        return Coordinate(latitude, longitude)
    }


    companion object {
        val zero = DoubleCoordinate(0.0, 0.0)

        fun from(coordinate: Coordinate): DoubleCoordinate {
            return DoubleCoordinate(coordinate.latitude, coordinate.longitude)
        }

        fun constrained(latitude: Double, longitude: Double): DoubleCoordinate {
            return DoubleCoordinate(latitude.coerceIn(-90.0, 90.0), Coordinate.toLongitude(longitude))
        }
    }
}
