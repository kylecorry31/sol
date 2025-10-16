package com.kylecorry.sol.science.shared

import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.time.ZonedDateTime

interface ISeasonService {
    fun getSeason(location: Coordinate, date: ZonedDateTime): Season
}