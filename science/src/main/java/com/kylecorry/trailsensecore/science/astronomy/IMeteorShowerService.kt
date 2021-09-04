package com.kylecorry.trailsensecore.science.astronomy

import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Coordinate
import java.time.Instant
import java.time.ZonedDateTime

interface IMeteorShowerService {
    fun getMeteorShower(location: Coordinate, date: ZonedDateTime): MeteorShowerPeak?
    fun getMeteorShowerAltitude(shower: MeteorShower, location: Coordinate, time: Instant): Float
    fun getMeteorShowerAzimuth(shower: MeteorShower, location: Coordinate, time: Instant): Bearing
}