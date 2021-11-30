package com.kylecorry.sol.science.geology.projections

import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.geometry.Size
import com.kylecorry.sol.science.geology.CoordinateBounds
import com.kylecorry.sol.units.Coordinate
import kotlin.math.absoluteValue
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln

// Adapted from https://stackoverflow.com/questions/2103924/mercator-longitude-and-latitude-calculations-to-x-and-y-on-a-cropped-map-of-the/10401734#10401734
class MercatorMapProjection(
    private val bounds: CoordinateBounds,
    private val mapSize: Size
) : IMapProjection {

    override fun toPixels(location: Coordinate): Vector2 {
        val deltaLongitude = (bounds.east - bounds.west).absoluteValue
        val x = (location.longitude - bounds.west) * (mapSize.width / deltaLongitude)

        val worldMapWidth = ((mapSize.width / deltaLongitude) * 360) / (2 * Math.PI);
        val mapOffsetY = (worldMapWidth / 2 * ln(
            (1 + sinDegrees(bounds.south)) / (1 - sinDegrees(bounds.south))
        ));
        val y = mapSize.height - ((worldMapWidth / 2 * ln(
            (1 + sinDegrees(location.latitude)) / (1 - sinDegrees(location.latitude))
        )) - mapOffsetY);

        return Vector2(x.toFloat(), y.toFloat())
    }

    override fun toCoordinate(pixel: Vector2): Coordinate {
        val deltaLongitude = (bounds.east - bounds.west).absoluteValue
        val worldMapWidth = mapSize.width / deltaLongitude * 360 / (2 * Math.PI);
        val mapOffsetY = (worldMapWidth / 2 * ln(
            (1 + sinDegrees(bounds.south)) / (1 - sinDegrees(bounds.south))
        ))
        val equatorY = mapSize.height + mapOffsetY
        val a = (equatorY - pixel.y) / worldMapWidth

        val lat = 180 / Math.PI * (2 * atan(exp(a)) - Math.PI / 2)
        val lon = bounds.west + pixel.x / mapSize.width * deltaLongitude
        return Coordinate(lat, lon)
    }

}