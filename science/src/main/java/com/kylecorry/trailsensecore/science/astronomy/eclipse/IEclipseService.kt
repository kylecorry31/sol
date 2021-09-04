package com.kylecorry.trailsensecore.science.astronomy.eclipse

import com.kylecorry.andromeda.core.units.Coordinate
import java.time.ZonedDateTime

interface IEclipseService {
    fun getNextEclipse(time: ZonedDateTime, location: Coordinate, type: EclipseType): Eclipse?
}