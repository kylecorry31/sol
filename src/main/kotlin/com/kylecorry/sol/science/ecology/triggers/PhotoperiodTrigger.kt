package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import java.time.Duration

class PhotoperiodTrigger(
    private val dayLength: Duration,
    private val above: Boolean = true
) : LifecycleEventTrigger {
    override fun isTriggered(factors: LifecycleEventFactors): Boolean {
        return if (above) {
            factors.lengthOfDay > dayLength
        } else {
            factors.lengthOfDay < dayLength
        }
    }
}
