package com.kylecorry.sol.science.oceanography

import java.time.ZonedDateTime

data class Tide(
    val time: ZonedDateTime,
    val type: TideType,
    val height: Float = if (type == TideType.High) defaultHighHeight else defaultLowHeight
) {
    companion object {

        private val defaultHighHeight = 1f
        private val defaultLowHeight = -1f

        fun high(time: ZonedDateTime, height: Float = defaultHighHeight): Tide {
            return Tide(time, TideType.High, height)
        }

        fun low(time: ZonedDateTime, height: Float = defaultLowHeight): Tide {
            return Tide(time, TideType.Low, height)
        }
    }
}