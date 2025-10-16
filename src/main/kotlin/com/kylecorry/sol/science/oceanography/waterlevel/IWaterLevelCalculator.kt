package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.time.ZonedDateTime

interface IWaterLevelCalculator {
    fun calculate(time: ZonedDateTime): Float
}