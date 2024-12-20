package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.science.astronomy.stars.AltitudeAzimuth
import com.kylecorry.sol.science.astronomy.stars.Star
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import java.time.ZonedDateTime

interface IStarService {
    fun getStarAltitude(
        star: Star,
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false
    ): Float

    fun getStarAzimuth(
        star: Star,
        time: ZonedDateTime,
        location: Coordinate
    ): Bearing

    /**
     * Get the color temperature of a star in Kelvin
     */
    fun getColorTemperature(star: Star): Float

    /**
     * Matches the readings to stars
     */
    fun plateSolve(
        readings: List<AltitudeAzimuth>,
        time: ZonedDateTime,
        approximateLocation: Coordinate? = null
    ): List<Pair<AltitudeAzimuth, Star>>

}