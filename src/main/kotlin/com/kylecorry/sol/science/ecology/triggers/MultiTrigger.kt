package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors

class MultiTrigger(private vararg val triggers: LifecycleEventTrigger, private val any: Boolean = false) : LifecycleEventTrigger {
    override fun isTriggered(factors: LifecycleEventFactors): Boolean {
        return if (any) {
            triggers.any { it.isTriggered(factors) }
        } else {
            triggers.all { it.isTriggered(factors) }
        }
    }

    override fun reset() {
        triggers.forEach { it.reset() }
    }
}