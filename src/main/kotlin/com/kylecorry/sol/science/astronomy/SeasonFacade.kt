package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.math.arithmetic.Arithmetic.wrap
import com.kylecorry.sol.science.shared.Season
import com.kylecorry.sol.units.Coordinate
import java.time.ZonedDateTime

internal object SeasonFacade {
    fun getSeason(location: Coordinate, date: ZonedDateTime): Season {
        val sl = wrap(SunFacade.getSolarLongitude(date), 0f, 360f)
        return when {
            sl >= OrbitalPosition.WinterSolstice.solarLongitude ->
                if (location.isNorthernHemisphere) Season.Winter else Season.Summer
            sl >= OrbitalPosition.AutumnalEquinox.solarLongitude ->
                if (location.isNorthernHemisphere) Season.Fall else Season.Spring
            sl >= OrbitalPosition.SummerSolstice.solarLongitude ->
                if (location.isNorthernHemisphere) Season.Summer else Season.Winter
            else -> if (location.isNorthernHemisphere) Season.Spring else Season.Fall
        }
    }
}
