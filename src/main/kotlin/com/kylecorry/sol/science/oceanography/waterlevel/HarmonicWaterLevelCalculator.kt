package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.science.oceanography.AstronomicalArgumentCalculator
import com.kylecorry.sol.science.oceanography.NodeFactorCalculator
import com.kylecorry.sol.science.oceanography.TidalHarmonic
import com.kylecorry.sol.science.oceanography.TideConstituent
import com.kylecorry.sol.time.Time
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class HarmonicWaterLevelCalculator(private val harmonics: List<TidalHarmonic>) :
    IWaterLevelCalculator {
    override fun calculate(time: ZonedDateTime): Float {
        // TODO: This is a hack to get the correct year for the astronomical arguments
        val year = if (time.year <= 2021){
            2021
        } else {
            2022
        }
        val start = ZonedDateTime.of(
            LocalDateTime.of(year, 1, 1, 0, 0),
            ZoneId.of("UTC")
        )
        val t = Duration.between(start, time).seconds / 3600f
        val heights = harmonics.map {
            val constituentPhase = AstronomicalArgumentCalculator.get(it.constituent, year)
            val nodeFactor = NodeFactorCalculator.get(
                it.constituent,
                year
            )
            nodeFactor * it.amplitude * SolMath.cosDegrees(it.constituent.speed * t + constituentPhase - it.phase)
        }
        return heights.sum()
    }
}