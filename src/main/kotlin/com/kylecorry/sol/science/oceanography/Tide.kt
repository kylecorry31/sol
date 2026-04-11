package com.kylecorry.sol.science.oceanography

import java.time.ZonedDateTime

data class Tide(
    val time: ZonedDateTime,
    val isHigh: Boolean,
    val height: Float? = null,
) {
    companion object {
        fun high(
            time: ZonedDateTime,
            height: Float? = null,
        ): Tide = Tide(time, true, height)

        fun low(
            time: ZonedDateTime,
            height: Float? = null,
        ): Tide = Tide(time, false, height)
    }
}
