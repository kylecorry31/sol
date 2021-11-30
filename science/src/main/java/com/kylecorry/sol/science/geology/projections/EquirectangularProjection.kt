package com.kylecorry.sol.science.geology.projections

import com.kylecorry.sol.math.SolMath.map
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.geometry.Size
import com.kylecorry.sol.science.geology.CoordinateBounds
import com.kylecorry.sol.units.Coordinate

class EquirectangularProjection(
    private val bounds: CoordinateBounds = CoordinateBounds.world,
    private val mapSize: Size = Size(1f, 1f)
) : IMapProjection {

    override fun toCoordinate(pixel: Vector2): Coordinate {
        val longitude =
            map(pixel.x.toDouble(), 0.0, mapSize.width.toDouble(), bounds.west, bounds.east)
        val latitude = map(
            mapSize.height - pixel.y.toDouble(),
            0.0,
            mapSize.height.toDouble(),
            bounds.south,
            bounds.north
        )

        return Coordinate(latitude, longitude)
    }

    override fun toPixels(location: Coordinate): Vector2 {
        val x = map(
            location.longitude,
            bounds.west,
            bounds.east,
            0.0,
            mapSize.width.toDouble()
        ).toFloat()
        val y = mapSize.height - map(
            location.latitude,
            bounds.south,
            bounds.north,
            0.0,
            mapSize.height.toDouble()
        ).toFloat()
        return Vector2(x, y)
    }
}