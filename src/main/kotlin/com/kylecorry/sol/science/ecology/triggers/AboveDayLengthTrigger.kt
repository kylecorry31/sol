package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import java.time.Duration

class AboveDayLengthTrigger(private val dayLength: Duration) : LifecycleEventTrigger {
    override fun isTriggered(factors: LifecycleEventFactors): Boolean {
        val aboveLength =
            factors.lengthOfDayHistory30Days.indexOfFirst { it >= dayLength }
        val belowLength =
            factors.lengthOfDayHistory30Days.indexOfFirst { it < dayLength }
        // Rises above day length
        return belowLength != -1 && aboveLength > belowLength
    }
}