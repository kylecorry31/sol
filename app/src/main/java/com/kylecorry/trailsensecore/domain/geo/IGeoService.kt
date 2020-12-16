package com.kylecorry.trailsensecore.domain.geo

interface IGeoService {

    fun getDeclination(coordinate: Coordinate, altitude: Float? = null, time: Long = System.currentTimeMillis()): Float

}