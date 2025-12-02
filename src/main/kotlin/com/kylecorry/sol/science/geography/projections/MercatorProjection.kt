package com.kylecorry.sol.science.geography.projections

import com.kylecorry.sol.math.SolMath.clamp
import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.toRadians
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.units.Coordinate
import kotlin.math.atan
import kotlin.math.ln
import kotlin.math.sinh

class MercatorProjection(private val scale: Float = 1f) : IMapProjection {
    override fun toCoordinate(pixel: Vector2): Coordinate {
        val longitude = (pixel.x / scale).toDegrees()
        val latitude = (atan(sinh(pixel.y / scale))).toDegrees()
        return Coordinate(
            clamp(latitude.toDouble(), -90.0, 90.0),
            Coordinate.toLongitude(longitude.toDouble())
        )
    }

    override fun toPixels(location: Coordinate): Vector2 {
        val x = scale * location.longitude.toRadians()
        val sinLat = sinDegrees(location.latitude)
        val y = 0.5 * ln((1 + sinLat) / (1 - sinLat))
        return Vector2(x.toFloat(), y.toFloat())
    }

    companion object {
        fun getScaleForLatitude(latitude: Double): Float {
            return cosDegrees(latitude).toFloat()
        }
    }

}