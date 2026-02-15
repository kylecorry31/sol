package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import java.time.Duration

class AboveDayLengthTrigger(private val dayLength: Duration) : LifecycleEventTrigger {
    override fun isTriggered(factors: LifecycleEventFactors): Boolean {
        val aboveLength =
            factors.lengthOfDay.history.indexOfFirst { it >= dayLength }
        val belowLength =
            factors.lengthOfDay.history.indexOfFirst { it < dayLength }
        // Rises above day length
        return belowLength != -1 && aboveLength > belowLength
    }
}