package com.kylecorry.sol.science.oceanography.waterlevel
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.analysis.Trigonometry
import com.kylecorry.sol.science.oceanography.Tide
import com.kylecorry.sol.time.Time.hoursBetween
import com.kylecorry.sol.time.ZonedDateTime

class RuleOfTwelfthsWaterLevelCalculator(
    private val first: Tide,
    private val second: Tide
) : IWaterLevelCalculator {

    private val wave by lazy {
        val firstVec = Vector2(getX(first.time), first.height ?: (if (first.isHigh) 1f else -1f))
        val secondVec =
            Vector2(getX(second.time), second.height ?: (if (second.isHigh) 1f else -1f))
        Trigonometry.connect(firstVec, secondVec)
    }

    override fun calculate(time: ZonedDateTime): Float {
        return wave.calculate(getX(time))
    }

    private fun getX(time: ZonedDateTime): Float {
        return hoursBetween(first.time, time)
    }

}