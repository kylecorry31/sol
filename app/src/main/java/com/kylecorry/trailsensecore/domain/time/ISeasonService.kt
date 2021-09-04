package com.kylecorry.trailsensecore.domain.time

import com.kylecorry.andromeda.core.units.Coordinate
import java.time.ZonedDateTime

interface ISeasonService {
    fun getSeason(location: Coordinate, date: ZonedDateTime): Season
}