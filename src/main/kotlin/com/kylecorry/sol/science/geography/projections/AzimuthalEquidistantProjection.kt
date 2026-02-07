package com.kylecorry.sol.science.geography.projections

import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.trigonometry.Trigonometry
import com.kylecorry.sol.math.trigonometry.Trigonometry.cosDegrees
import com.kylecorry.sol.math.trigonometry.Trigonometry.sinDegrees
import com.kylecorry.sol.science.geography.Geography
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import kotlin.math.atan2

class AzimuthalEquidistantProjection(
    private val centerLocation: Coordinate,
    private val centerPixel: Vector2 = Vector2.zero,
    private val scale: Float = 1f,
    private val isYFlipped: Boolean = false
) :
    IMapProjection {

    override fun toPixels(location: Coordinate): Vector2 {
        val navigation = Geography.navigate(centerLocation, location)
        val angle = Trigonometry.toUnitAngle(navigation.direction.value, 90f, false)
        val pixelDistance = navigation.distance * scale
        val xDiff = cosDegrees(angle) * pixelDistance
        val yDiff = sinDegrees(angle) * pixelDistance
        return Vector2(centerPixel.x + xDiff, centerPixel.y + if (isYFlipped) -1 * yDiff else yDiff)
    }

    override fun toPixels(latitude: Double, longitude: Double): Vector2 {
        return toPixels(Coordinate(latitude, longitude))
    }

    override fun toCoordinate(pixel: Vector2): Coordinate {
        val angle = Trigonometry.toUnitAngle(
            atan2(
                if (isYFlipped) {
                    centerPixel.y - pixel.y
                } else {
                    pixel.y - centerPixel.y
                }, pixel.x - centerPixel.x
            ).toDegrees(),
            90f,
            false
        )
        val distance = centerPixel.distanceTo(pixel) / scale
        return centerLocation.plus(distance.toDouble(), Bearing.from(angle))
    }
}