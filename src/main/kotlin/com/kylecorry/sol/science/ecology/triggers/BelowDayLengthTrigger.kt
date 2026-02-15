package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import java.time.Duration

class BelowDayLengthTrigger(private val dayLength: Duration) : LifecycleEventTrigger {
    override fun isTriggered(factors: LifecycleEventFactors): Boolean {
        val aboveLength =
            factors.lengthOfDay.history.indexOfFirst { it > dayLength }
        val belowLength =
            factors.lengthOfDay.history.indexOfFirst { it <= dayLength }
        // Drops below day length
        return aboveLength != -1 && belowLength > aboveLength
    }
}