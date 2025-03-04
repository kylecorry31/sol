package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.SolMath.deltaAngle
import com.kylecorry.sol.math.SolMath.isCloseTo
import com.kylecorry.sol.units.*
import kotlin.math.max

class CoordinateBounds(val north: Double, val east: Double, val south: Double, val west: Double) :
    IGeoArea {

    val northWest = Coordinate(north, west)
    val southWest = Coordinate(south, west)
    val northEast = Coordinate(north, east)
    val southEast = Coordinate(south, east)

    val center: Coordinate
        get() {
            val lat = (north + south) / 2
            val lon = if (west <= east) {
                (west + east) / 2
            } else {
                (west + east + 360) / 2
            }

            return Coordinate(lat, Coordinate.toLongitude(lon))
        }

    fun height(): Quantity<Distance> {
        return Distance.meters(
            max(
                northWest.distanceTo(southWest),
                northEast.distanceTo(southEast)
            )
        )
    }

    fun width(): Quantity<Distance> {
        if (0.0 in south..north) {
            return Distance.meters(Coordinate(0.0, west).distanceTo(Coordinate(0.0, east)))
        }

        return Distance.meters(
            max(
                southEast.distanceTo(southWest),
                northEast.distanceTo(northWest)
            )
        )
    }

    override fun contains(location: Coordinate): Boolean {
        val containsLatitude = location.latitude in south..north

        val containsLongitude = if (isCloseTo(west, world.west, 0.0001) && isCloseTo(east, world.east, 0.0001)) {
            true
        } else if (east < 0 && west > 0) {
            location.longitude >= west || location.longitude <= east
        } else {
            location.longitude in west..east
        }

        return containsLatitude && containsLongitude
    }

    fun intersects(other: CoordinateBounds): Boolean {
        val inOther =
            other.contains(northEast) || other.contains(northWest) || other.contains(southEast) || other.contains(
                southWest
            )

        val otherIn =
            contains(other.northEast) || contains(other.northWest) || contains(other.southEast) || contains(
                other.southWest
            )

        return inOther || otherIn
    }

    companion object {

        val empty = CoordinateBounds(0.0, 0.0, 0.0, 0.0)
        val world = CoordinateBounds(90.0, 180.0, -90.0, -180.0)

        fun from(geofence: Geofence): CoordinateBounds {
            val north =
                geofence.center.plus(geofence.radius, Bearing.from(CompassDirection.North)).latitude
            val south =
                geofence.center.plus(geofence.radius, Bearing.from(CompassDirection.South)).latitude
            val east =
                geofence.center.plus(geofence.radius, Bearing.from(CompassDirection.East)).longitude
            val west =
                geofence.center.plus(geofence.radius, Bearing.from(CompassDirection.West)).longitude

            return CoordinateBounds(north, east, south, west)
        }

        fun from(points: List<Coordinate>): CoordinateBounds {
            val west = getWestLongitudeBound(points) ?: return empty
            val east = getEastLongitudeBound(points) ?: return empty
            val north = getNorthLatitudeBound(points) ?: return empty
            val south = getSouthLatitudeBound(points) ?: return empty

            val minLongitude = points.minByOrNull { it.longitude }?.longitude
            val maxLongitude = points.maxByOrNull { it.longitude }?.longitude

            // This is to support the case where the whole map is shown
            if (isCloseTo(minLongitude ?: 0.0, -180.0, 0.001) && isCloseTo(maxLongitude ?: 0.0, 180.0, 0.001)) {
                return CoordinateBounds(north, maxLongitude!!, south, minLongitude!!)
            }

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