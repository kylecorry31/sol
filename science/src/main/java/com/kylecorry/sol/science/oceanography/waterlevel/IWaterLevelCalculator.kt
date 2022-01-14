package com.kylecorry.sol.science.oceanography.waterlevel

import java.time.ZonedDateTime

interface IWaterLevelCalculator {
    fun calculate(time: ZonedDateTime): Float
}