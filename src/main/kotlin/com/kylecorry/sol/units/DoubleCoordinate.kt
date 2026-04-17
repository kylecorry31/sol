package com.kylecorry.sol.units


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
