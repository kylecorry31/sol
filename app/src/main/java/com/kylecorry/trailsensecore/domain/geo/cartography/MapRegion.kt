package com.kylecorry.trailsensecore.domain.geo.cartography

import com.kylecorry.andromeda.core.units.Coordinate

data class MapRegion(val north: Double, val east: Double, val south: Double, val west: Double){

    val northWest = Coordinate(north, west)
    val southWest = Coordinate(south, west)
    val northEast = Coordinate(north, east)
    val southEast = Coordinate(south, east)

    val center: Coordinate
        get() {
            val distance = northWest.distanceTo(southEast)
            val bearing = northWest.bearingTo(southEast)
            return northWest.plus(distance.toDouble() / 2, bearing)
        }

    fun contains(location: Coordinate): Boolean {
        val containsLatitude = location.latitude in south..north

        val containsLongitude = if (east < 0 && west > 0){
            location.longitude >= west || location.longitude <= east
        } else {
            location.longitude in west..east
        }

        return containsLatitude && containsLongitude
    }
}
