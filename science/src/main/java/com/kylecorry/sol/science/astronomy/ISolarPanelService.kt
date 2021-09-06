package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.units.Coordinate
import java.time.ZonedDateTime

interface ISolarPanelService {

    /**
     * Gets the solar radiation for the given time in kW/m^2
     */
    fun getSolarRadiation(
        date: ZonedDateTime,
        location: Coordinate,
        position: SolarPanelPosition
    ): Double

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