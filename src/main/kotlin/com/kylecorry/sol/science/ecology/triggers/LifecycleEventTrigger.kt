package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors

interface LifecycleEventTrigger {
    fun isTriggered(factors: LifecycleEventFactors): Boolean
}