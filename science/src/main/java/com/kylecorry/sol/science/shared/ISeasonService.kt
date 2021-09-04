package com.kylecorry.sol.science.shared

import com.kylecorry.sol.units.Coordinate
import java.time.ZonedDateTime

interface ISeasonService {
    fun getSeason(location: Coordinate, date: ZonedDateTime): Season
}