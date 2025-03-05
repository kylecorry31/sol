package com.kylecorry.sol.science.oceanography

import com.kylecorry.sol.math.optimization.IExtremaFinder
import com.kylecorry.sol.math.optimization.NoisyExtremaFinder
import com.kylecorry.sol.science.oceanography.waterlevel.IWaterLevelCalculator
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.Quantity
import java.time.Duration
import java.time.ZonedDateTime

interface IOceanographyService {

    fun getDepth(
        pressure: Pressure,
        seaLevelPressure: Pressure,
        isSaltWater: Boolean = true
    ): Quantity<Distance>

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

    /**
     * Gets the approximate lunitidal interval for the given high tide time. If the location is provided, this will be the local lunitidal interval.
     * @param highTideTime The time of the high tide (you can pass in a low tide if you want to calculate the low lunitidal interval)
     * @param location The location of the tide. If not provided, the interval will be calculated for the prime meridian.
     * @return The lunitidal interval or null if it could not be calculated
     */
    fun getLunitidalInterval(highTideTime: ZonedDateTime, location: Coordinate = Coordinate.zero): Duration?

    /**
     * Gets the approximate mean lunitidal interval for the given high tide times. If the location is provided, this will be the local lunitidal interval.
     * @param highTideTimes The times of the high tides (you can pass in low tides if you want to calculate the low lunitidal interval)
     * @param location The location of the tide. If not provided, the interval will be calculated for the prime meridian.
     * @return The mean lunitidal interval or null if it could not be calculated
     */
    fun getMeanLunitidalInterval(
        highTideTimes: List<ZonedDateTime>,
        location: Coordinate = Coordinate.zero
    ): Duration?
}