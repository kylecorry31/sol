package com.kylecorry.sol.science.oceanography

import java.time.ZonedDateTime

data class Tide(val time: ZonedDateTime, val type: TideType) {
    companion object {
        fun high(time: ZonedDateTime): Tide {
            return Tide(time, TideType.High)
        }

        fun low(time: ZonedDateTime): Tide {
            return Tide(time, TideType.Low)
        }
    }
}