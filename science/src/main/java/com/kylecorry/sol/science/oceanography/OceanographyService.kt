package com.kylecorry.sol.science.oceanography

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.optimization.IExtremaFinder
import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.science.astronomy.moon.MoonTruePhase
import com.kylecorry.sol.science.geology.Geology
import com.kylecorry.sol.science.oceanography.waterlevel.IWaterLevelCalculator
import com.kylecorry.sol.units.*
import java.time.*

class OceanographyService : IOceanographyService {

    override fun getTidalRange(time: ZonedDateTime): TidalRange {
        for (i in 0..3) {
            val phase = Astronomy.getMoonPhase(time.minusDays(i.toLong()))

            when (phase.phase) {
                MoonTruePhase.New, MoonTruePhase.Full -> {
                    return TidalRange.Spring
                }
                MoonTruePhase.FirstQuarter, MoonTruePhase.ThirdQuarter -> {
                    return TidalRange.Neap
                }
                else -> {
                    // Do nothing
                }
            }
        }

        return TidalRange.Normal
    }

    override fun getTides(
        waterLevelCalculator: IWaterLevelCalculator,
        start: ZonedDateTime,
        end: ZonedDateTime,
        extremaFinder: IExtremaFinder
    ): List<Tide> {
        val range = Duration.between(start, end).toMinutes()
        val extrema = extremaFinder.find(Range(0.0, range.toDouble())) {
            waterLevelCalculator.calculate(start.plusMinutes(it.toLong())).toDouble()
        }
        return extrema.map { Tide(start.plusMinutes(it.point.x.toLong()), it.isHigh, it.point.y) }
    }

    override fun getDepth(
        pressure: Pressure,
        seaLevelPressure: Pressure,
        isSaltWater: Boolean
    ): Distance {
        if (pressure <= seaLevelPressure) {
            return Distance(0f, DistanceUnits.Meters)
        }

        val waterDensity = if (isSaltWater) DENSITY_SALT_WATER else DENSITY_FRESH_WATER
        val pressureDiff =
            pressure.convertTo(PressureUnits.Hpa).pressure - seaLevelPressure.convertTo(
                PressureUnits.Hpa
            ).pressure

        return Distance(
            pressureDiff * 100 / (Geology.GRAVITY * waterDensity),
            DistanceUnits.Meters
        )
    }

    companion object {
        const val DENSITY_SALT_WATER = 1023.6f
        const val DENSITY_FRESH_WATER = 997.0474f
    }

}