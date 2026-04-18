package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.science.astronomy.eclipse.Eclipse
import com.kylecorry.sol.science.astronomy.eclipse.EclipseType
import com.kylecorry.sol.science.astronomy.eclipse.lunar.PartialLunarEclipseCalculator
import com.kylecorry.sol.science.astronomy.eclipse.lunar.TotalLunarEclipseCalculator
import com.kylecorry.sol.science.astronomy.eclipse.solar.SolarEclipseCalculator
import com.kylecorry.sol.units.Coordinate
import java.time.Duration
import java.time.ZonedDateTime

internal object EclipseFacade {
    fun getNextEclipse(
        time: ZonedDateTime,
        location: Coordinate,
        type: EclipseType,
        maxSearch: Duration? = null
    ): Eclipse? {
        val calculator = when (type) {
            EclipseType.PartialLunar -> PartialLunarEclipseCalculator()
            EclipseType.TotalLunar -> TotalLunarEclipseCalculator()
            EclipseType.Solar -> SolarEclipseCalculator(maxDuration = maxSearch)
        }
        return calculator.getNextEclipse(time.toInstant(), location)
    }

    fun getEclipseMagnitude(time: ZonedDateTime, location: Coordinate, type: EclipseType): Float? {
        val calculator = when (type) {
            EclipseType.PartialLunar -> PartialLunarEclipseCalculator()
            EclipseType.TotalLunar -> TotalLunarEclipseCalculator()
            EclipseType.Solar -> SolarEclipseCalculator()
        }

        return calculator.getMagnitude(time.toInstant(), location)
    }

    fun getEclipseObscuration(time: ZonedDateTime, location: Coordinate, type: EclipseType): Float? {
        val calculator = when (type) {
            EclipseType.PartialLunar -> PartialLunarEclipseCalculator()
            EclipseType.TotalLunar -> TotalLunarEclipseCalculator()
            EclipseType.Solar -> SolarEclipseCalculator()
        }

        return calculator.getObscuration(time.toInstant(), location)
    }
}
