package com.kylecorry.sol.science.geology.projections

import com.kylecorry.sol.math.SolMath.clamp
import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.toRadians
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.units.Coordinate

class CylindricalEquidistantProjection(private val scale: Float = 1f) : IMapProjection {

    override fun toCoordinate(pixel: Vector2): Coordinate {
        val longitude = (pixel.x / scale).toDouble().toDegrees()
        val latitude = pixel.y.toDouble().toDegrees()
        return Coordinate(clamp(latitude, -180.0, 180.0), clamp(longitude, -90.0, 90.0))
    }

    override fun toPixels(location: Coordinate): Vector2 {
        val x = location.longitude.toRadians() * scale
        val y = location.latitude.toRadians()
        return Vector2(x.toFloat(), y.toFloat())
    }

    companion object {
        fun getScaleForLatitude(latitude: Double): Float {
            return cosDegrees(latitude).toFloat()
        }
    }

}