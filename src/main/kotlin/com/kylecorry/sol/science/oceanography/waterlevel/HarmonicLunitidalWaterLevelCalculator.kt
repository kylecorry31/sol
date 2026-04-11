package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.science.oceanography.TidalHarmonic
import com.kylecorry.sol.science.oceanography.TideConstituent
import com.kylecorry.sol.time.Time
import com.kylecorry.sol.units.Coordinate
import java.time.Duration
import java.time.ZonedDateTime

class HarmonicLunitidalWaterLevelCalculator(
    lunitidalInterval: Duration,
    location: Coordinate = Coordinate.zero
) : IWaterLevelCalculator {

    private val harmonic = TidalHarmonic(
        TideConstituent.M2,
        1f,
        ((Time.hours(lunitidalInterval) - location.longitude / 15) * TideConstituent.M2.speed).toFloat()
    )

    private val harmonicCalculator = HarmonicWaterLevelCalculator(listOf(harmonic))

    override fun calculate(time: ZonedDateTime): Float {
        return harmonicCalculator.calculate(time)
    }
}