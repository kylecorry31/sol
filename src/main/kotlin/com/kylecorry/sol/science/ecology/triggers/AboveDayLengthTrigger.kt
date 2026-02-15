package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import java.time.Duration

class AboveDayLengthTrigger(private val dayLength: Duration) : LifecycleEventTrigger {
    override fun isTriggered(factors: LifecycleEventFactors): Boolean {
        return factors.lengthOfDay.current > dayLength
    }
}