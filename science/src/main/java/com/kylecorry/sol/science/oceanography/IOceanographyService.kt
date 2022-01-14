package com.kylecorry.sol.science.oceanography

import com.kylecorry.sol.math.optimization.IExtremaFinder
import com.kylecorry.sol.math.optimization.NoisyExtremaFinder
import com.kylecorry.sol.science.oceanography.waterlevel.IWaterLevelCalculator
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Pressure
import java.time.ZonedDateTime

interface IOceanographyService {

    fun getDepth(
        pressure: Pressure,
        seaLevelPressure: Pressure,
        isSaltWater: Boolean = true
    ): Distance

    fun getTidalRange(time: ZonedDateTime): TidalRange

    /**
     * Gets the tides for the day
     */
    fun getTides(
        waterLevelCalculator: IWaterLevelCalculator,
        start: ZonedDateTime,
        end: ZonedDateTime,
        extremaFinder: IExtremaFinder = NoisyExtremaFinder(1.0, 10)
    ): List<Tide>
}