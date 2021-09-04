package com.kylecorry.trailsensecore.science.shared

import com.kylecorry.trailsensecore.units.Coordinate
import java.time.ZonedDateTime

interface ISeasonService {
    fun getSeason(location: Coordinate, date: ZonedDateTime): Season
}