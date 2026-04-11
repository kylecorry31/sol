package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import java.time.Duration
import java.time.LocalDate

class DurationTrigger(
    private val baseTrigger: LifecycleEventTrigger,
    private val duration: Duration,
    private val resetWhenBaseNotTriggered: Boolean = false
) : LifecycleEventTrigger {

    private var triggerDate: LocalDate? = null

    override fun isTriggered(factors: LifecycleEventFactors): Boolean {
        val isBaseTriggered = baseTrigger.isTriggered(factors)
        if (isBaseTriggered && triggerDate == null) {
            triggerDate = factors.date.plusDays(duration.toDays())
        }
        if (!isBaseTriggered && resetWhenBaseNotTriggered) {
            triggerDate = null
        }

        val currentTriggerDate = triggerDate
        return currentTriggerDate != null && factors.date >= currentTriggerDate
    }

    override fun reset() {
        triggerDate = null
        baseTrigger.reset()
    }
}