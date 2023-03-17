package com.kylecorry.sol.science.astronomy.eclipse

import com.kylecorry.sol.units.Coordinate
import java.time.Duration
import java.time.ZonedDateTime

interface IEclipseService {
    fun getNextEclipse(
        time: ZonedDateTime,
        location: Coordinate,
        type: EclipseType,
        maxSearch: Duration? = null
    ): Eclipse?
}