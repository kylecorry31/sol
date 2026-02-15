package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature

class BelowTemperatureTrigger(
    averageLowTemperature: Temperature = Temperature.celsius(0f),
) : LifecycleEventTrigger {

    private val averageLowC = averageLowTemperature.celsius().value

    override fun isTriggered(factors: LifecycleEventFactors): Boolean {
        return factors.temperature.current.start.celsius().value < averageLowC
    }

}