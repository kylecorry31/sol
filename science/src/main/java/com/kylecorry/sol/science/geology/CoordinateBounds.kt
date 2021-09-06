package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.SolMath.deltaAngle
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import kotlin.math.max

class CoordinateBounds(val north: Double, val east: Double, val south: Double, val west: Double): IGeoArea {

    val northWest = Coordinate(north, west)
    val southWest = Coordinate(south, west)
    val northEast = Coordinate(north, east)
    val southEast = Coordinate(south, east)

    val center: Coordinate
        get() {
            val lat = (north + south) / 2
            val lon = west + deltaAngle(west.toFloat() + 180, east.toFloat() + 180).toDouble() / 2
            return Coordinate(lat, lon)
        }

    fun height(): Distance {
        return Distance.meters(max(northWest.distanceTo(southWest), northEast.distanceTo(southEast)))
    }

    fun width(): Distance {
        if (0.0 in south..north) {
            return Distance.meters(Coordinate(0.0, west).distanceTo(Coordinate(0.0, east)))
        }

        return Distance.meters(max(southEast.distanceTo(southWest), northEast.distanceTo(northWest)))
    }

    override fun contains(location: Coordinate): Boolean {
        val containsLatitude = location.latitude in south..north

        val containsLongitude = if (east < 0 && west > 0) {
            location.longitude >= west || location.longitude <= east
        } else {
            location.longitude in west..east
        }

        return containsLatitude && containsLongitude
    }

    companion object {

        val empty = CoordinateBounds(0.0, 0.0, 0.0, 0.0)

        fun from(points: List<Coordinate>): CoordinateBounds {
            val west = getWestLongitudeBound(points) ?: return empty
            val east = getEastLongitudeBound(points) ?: return empty
            val north = getNorthLatitudeBound(points) ?: return empty
            val south = getSouthLatitudeBound(points) ?: return empty
            return CoordinateBounds(north, east, south, west)
        }


        private fun getWestLongitudeBound(locations: List<Coordinate>): Double? {
            val first = locations.firstOrNull() ?: return null
            return locations.minByOrNull {
                deltaAngle(
                        first.longitude.toFloat() + 180,
                        it.longitude.toFloat() + 180
                )
            }?.longitude
        }

        private fun getEastLongitudeBound(locations: List<Coordinate>): Double? {
            val first = locations.firstOrNull() ?: return null
            return locations.maxByOrNull {
                deltaAngle(
                        first.longitude.toFloat() + 180,
                        it.longitude.toFloat() + 180
                )
            }?.longitude
        }

        private fun getSouthLatitudeBound(locations: List<Coordinate>): Double? {
            return locations.minByOrNull { it.latitude }?.latitude
        }

        private fun getNorthLatitudeBound(locations: List<Coordinate>): Double? {
            return locations.maxByOrNull { it.latitude }?.latitude
        }
    }
}