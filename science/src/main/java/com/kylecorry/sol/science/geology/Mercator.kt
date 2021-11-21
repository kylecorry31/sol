package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.SolMath.deltaAngle
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.units.Coordinate
import kotlin.math.absoluteValue
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln

// Adapted from https://stackoverflow.com/questions/2103924/mercator-longitude-and-latitude-calculations-to-x-and-y-on-a-cropped-map-of-the/10401734#10401734
internal class Mercator {

    fun toPixel(
        coordinate: Coordinate,
        bounds: CoordinateBounds,
        size: Pair<Float, Float>
    ): Vector2 {
        val deltaLongitude =
            deltaAngle(bounds.east.toFloat(), bounds.west.toFloat()).absoluteValue.toDouble()
        val x = (coordinate.longitude - bounds.west) * (size.first / deltaLongitude)

        val worldMapWidth = ((size.first / deltaLongitude) * 360) / (2 * Math.PI);
        val mapOffsetY = (worldMapWidth / 2 * ln(
            (1 + sinDegrees(bounds.south)) / (1 - sinDegrees(bounds.south))
        ));
        val y = size.second - ((worldMapWidth / 2 * ln(
            (1 + sinDegrees(coordinate.latitude)) / (1 - sinDegrees(coordinate.latitude))
        )) - mapOffsetY);

        return Vector2(x.toFloat(), y.toFloat())
    }

    fun fromPixel(
        pixel: Vector2,
        bounds: CoordinateBounds,
        size: Pair<Float, Float>
    ): Coordinate {
        val deltaLongitude =
            deltaAngle(bounds.east.toFloat(), bounds.west.toFloat()).absoluteValue.toDouble()
        val worldMapWidth = size.first / deltaLongitude * 360 / (2 * Math.PI);
        val mapOffsetY = (worldMapWidth / 2 * ln(
            (1 + sinDegrees(bounds.south)) / (1 - sinDegrees(bounds.south))
        ))
        val equatorY = size.second + mapOffsetY
        val a = (equatorY - pixel.y) / worldMapWidth

        val lat = 180 / Math.PI * (2 * atan(exp(a)) - Math.PI / 2)
        val lon = bounds.west + pixel.x / size.first * deltaLongitude
        return Coordinate(lat, lon)
    }

}