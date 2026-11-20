package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.arithmetic.Arithmetic.isCloseTo
import com.kylecorry.sol.math.trigonometry.Trigonometry.deltaAngle
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.CompassDirection
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.max

data class CoordinateBounds(val north: Double, val east: Double, val south: Double, val west: Double) :
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

    fun height(): Distance {
        return Distance.meters(
            max(
                northWest.distanceTo(southWest),
                northEast.distanceTo(southEast)
            )
        )
    }

    fun heightDegrees(): Double {
        return (north - south).absoluteValue
    }

    fun widthDegrees(): Double {
        if (containsAllLongitudes()) {
            return 360.0
        }

        return (if (east >= west) {
            east - west
        } else {
            (180 - west) + (east + 180)
        }).absoluteValue
    }

    fun width(): Distance {
        if (0.0 in south..north) {
            return Distance.meters(widthAtLatitude(0.0))
        }

        return Distance.meters(max(widthAtLatitude(south), widthAtLatitude(north)))
    }

    /**
     * Distance along a parallel, split into segments of at most 90 degrees so that
     * widths of 180 degrees or more aren't measured the short way around.
     */
    private fun widthAtLatitude(latitude: Double): Float {
        val degrees = widthDegrees()
        val segments = ceil(degrees / 90).toInt().coerceAtLeast(1)
        var total = 0f
        for (i in 0..<segments) {
            val from = Coordinate(latitude, west + degrees * i / segments)
            val to = Coordinate(latitude, west + degrees * (i + 1) / segments)
            total += from.distanceTo(to)
        }
        return total
    }

    private fun containsAllLongitudes(): Boolean {
        return isCloseTo(west, world.west, 0.0001) && isCloseTo(east, world.east, 0.0001)
    }

    override fun contains(location: Coordinate): Boolean {
        return containsLatitude(location.latitude) && containsLongitude(location.longitude)
    }

    fun containsLatitude(latitude: Double): Boolean {
        return latitude in south..north
    }

    fun containsLongitude(longitude: Double): Boolean {
        return if (containsAllLongitudes()) {
            true
        } else if (east < 0 && west > 0) {
            longitude >= west || longitude <= east
        } else {
            longitude in west..east
        }
    }

    fun contains(other: CoordinateBounds): Boolean {
        return contains(other.northEast) &&
                contains(other.northWest) &&
                contains(other.southEast) &&
                contains(other.southWest) &&
                contains(other.center)
    }

    fun intersects(other: CoordinateBounds): Boolean {
        if (south > other.north || other.south > north) {
            return false
        }

        val union = from(
            listOf(
                northEast, northWest, southEast, southWest,
                other.northEast, other.northWest, other.southEast, other.southWest
            )
        )
        return union.widthDegrees() <= (widthDegrees() + other.widthDegrees())
    }

    fun grow(percent: Float): CoordinateBounds {
        val latDelta = heightDegrees() * percent
        val lonDelta = widthDegrees() * percent

        val newNorth = (north + latDelta).coerceAtMost(90.0)
        val newSouth = (south - latDelta).coerceAtLeast(-90.0)

        val newWest = if (containsAllLongitudes()) {
            world.west
        } else {
            Coordinate.toLongitude(west - lonDelta)
        }

        val newEast = if (containsAllLongitudes()) {
            world.east
        } else {
            Coordinate.toLongitude(east + lonDelta)
        }

        return CoordinateBounds(newNorth, newEast, newSouth, newWest)
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

        fun from(points: List<Coordinate>, checkForFullWorld: Boolean = true): CoordinateBounds {
            val west = getWestLongitudeBound(points) ?: return empty
            val east = getEastLongitudeBound(points) ?: return empty
            val north = getNorthLatitudeBound(points) ?: return empty
            val south = getSouthLatitudeBound(points) ?: return empty

            val minLongitude = points.minByOrNull { it.longitude }?.longitude ?: 0.0
            val maxLongitude = points.maxByOrNull { it.longitude }?.longitude ?: 0.0

            // This is to support the case where the whole map is shown
            if (checkForFullWorld && isCloseTo(minLongitude, -180.0, 0.001) && isCloseTo(
                    maxLongitude,
                    180.0,
                    0.001
                )
            ) {
                return CoordinateBounds(north, maxLongitude, south, minLongitude)
            }

            return CoordinateBounds(north, east, south, west)
        }


        private fun getWestLongitudeBound(locations: List<Coordinate>): Double? {
            val first = locations.firstOrNull() ?: return null
            return locations.minByOrNull {
                deltaAngle(
                    first.longitude + 180,
                    it.longitude + 180
                )
            }?.longitude
        }

        private fun getEastLongitudeBound(locations: List<Coordinate>): Double? {
            val first = locations.firstOrNull() ?: return null
            return locations.maxByOrNull {
                deltaAngle(
                    first.longitude + 180,
                    it.longitude + 180
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
