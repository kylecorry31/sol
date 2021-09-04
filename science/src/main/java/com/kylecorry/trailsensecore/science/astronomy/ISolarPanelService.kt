package com.kylecorry.trailsensecore.science.astronomy

import com.kylecorry.trailsensecore.units.Coordinate
import java.time.ZonedDateTime

interface ISolarPanelService {
    fun getBestSolarPanelPositionForRestOfDay(
        start: ZonedDateTime,
        location: Coordinate
    ): SolarPanelPosition

    fun getBestSolarPanelPositionForDay(
        date: ZonedDateTime,
        location: Coordinate
    ): SolarPanelPosition

    fun getBestSolarPanelPositionForTime(
        time: ZonedDateTime,
        location: Coordinate
    ): SolarPanelPosition
}