package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors

class MultiTrigger(private vararg val triggers: LifecycleEventTrigger) : LifecycleEventTrigger {
    override fun isTriggered(factors: LifecycleEventFactors): Boolean {
        return triggers.all { it.isTriggered(factors) }
    }
}