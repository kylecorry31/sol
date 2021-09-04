package com.kylecorry.trailsensecore.science.astronomy.meteors

import com.kylecorry.trailsensecore.units.Bearing
import com.kylecorry.trailsensecore.units.Coordinate
import java.time.Instant
import java.time.ZonedDateTime

interface IMeteorShowerService {
    fun getMeteorShower(location: Coordinate, date: ZonedDateTime): MeteorShowerPeak?
    fun getMeteorShowerAltitude(shower: MeteorShower, location: Coordinate, time: Instant): Float
    fun getMeteorShowerAzimuth(shower: MeteorShower, location: Coordinate, time: Instant): Bearing
}