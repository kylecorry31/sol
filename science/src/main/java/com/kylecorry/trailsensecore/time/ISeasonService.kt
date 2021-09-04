package com.kylecorry.trailsensecore.time

import com.kylecorry.trailsensecore.units.Coordinate
import java.time.ZonedDateTime

interface ISeasonService {
    fun getSeason(location: Coordinate, date: ZonedDateTime): Season
}