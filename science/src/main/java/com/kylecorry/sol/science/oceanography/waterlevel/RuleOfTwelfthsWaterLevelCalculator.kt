package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.analysis.WaveService
import com.kylecorry.sol.science.oceanography.Tide
import com.kylecorry.sol.time.Time.hoursBetween
import java.time.ZonedDateTime

class RuleOfTwelfthsWaterLevelCalculator(
    private val first: Tide,
    private val second: Tide
) :
    IWaterLevelCalculator {

    private val waveService = WaveService()

    private val wave by lazy {
        val firstVec = Vector2(getX(first.time), first.height ?: (if (first.isHigh) 1f else -1f))
        val secondVec = Vector2(getX(second.time), second.height ?: (if (second.isHigh) 1f else -1f))
        waveService.connect(firstVec, secondVec)
    }

    override fun calculate(time: ZonedDateTime): Float {
        return wave.calculate(getX(time))
    }

    private fun getX(time: ZonedDateTime): Float {
        return hoursBetween(first.time, time)
    }

}