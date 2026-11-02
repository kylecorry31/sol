package com.kylecorry.sol.science.geophysics

import com.kylecorry.sol.science.geology.Geology
import com.kylecorry.sol.units.Coordinate
import kotlin.math.cos

internal object VerticalDeflectionCalculator {

    fun toGeodetic(astronomic: Coordinate, deflection: VerticalDeflection): Coordinate {
        val latitude = astronomic.latitude - deflection.xi / ARC_SECONDS_PER_DEGREE
        val longitude = astronomic.longitude -
                deflection.eta / ARC_SECONDS_PER_DEGREE / cos(Math.toRadians(latitude))
        return Coordinate(latitude, longitude)
    }

    fun toAstronomic(geodetic: Coordinate, deflection: VerticalDeflection): Coordinate {
        val latitude = geodetic.latitude + deflection.xi / ARC_SECONDS_PER_DEGREE
        val longitude = geodetic.longitude +
                deflection.eta / ARC_SECONDS_PER_DEGREE / cos(Math.toRadians(geodetic.latitude))
        return Coordinate(latitude, longitude)
    }

    fun fromGeoid(stepDegrees: Double, geoid: (Coordinate) -> Double): VerticalDeflectionModel {
        return VerticalDeflectionModel { location ->
            val north = geoid(Coordinate(location.latitude + stepDegrees, location.longitude))
            val south = geoid(Coordinate(location.latitude - stepDegrees, location.longitude))
            val east = geoid(Coordinate(location.latitude, location.longitude + stepDegrees))
            val west = geoid(Coordinate(location.latitude, location.longitude - stepDegrees))
            val dy = 2 * Math.toRadians(stepDegrees) * Geology.EARTH_AVERAGE_RADIUS
            val dx = dy * cos(Math.toRadians(location.latitude))
            VerticalDeflection(
                Math.toDegrees(-(north - south) / dy) * ARC_SECONDS_PER_DEGREE,
                Math.toDegrees(-(east - west) / dx) * ARC_SECONDS_PER_DEGREE
            )
        }
    }

    private const val ARC_SECONDS_PER_DEGREE = 3600.0
}
