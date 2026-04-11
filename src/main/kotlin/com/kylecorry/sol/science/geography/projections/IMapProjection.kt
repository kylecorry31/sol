package com.kylecorry.sol.science.geography.projections

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.units.Coordinate

interface IMapProjection {

    fun toPixels(location: Coordinate): Vector2

    fun toPixels(latitude: Double, longitude: Double): Vector2

    fun toCoordinate(pixel: Vector2): Coordinate

}