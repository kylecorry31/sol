package com.kylecorry.sol.science.astronomy.meteors

import com.kylecorry.sol.science.astronomy.units.CelestialObservation
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import java.time.Instant
import java.time.ZonedDateTime

interface IMeteorShowerService {
    fun getMeteorShower(location: Coordinate, date: ZonedDateTime): MeteorShowerPeak?
    fun getMeteorShowerAltitude(shower: MeteorShower, location: Coordinate, time: Instant): Float
    fun getMeteorShowerAzimuth(shower: MeteorShower, location: Coordinate, time: Instant): Bearing

    /**
     * Get a list of meteor showers which are active.
     * This does not check the time of day, so the shower may not currently be visible.
     */
    fun getActiveMeteorShowers(location: Coordinate, date: ZonedDateTime): List<MeteorShowerPeak>
    fun getMeteorShowerPosition(
        shower: MeteorShower,
        location: Coordinate,
        time: Instant
    ): CelestialObservation
}