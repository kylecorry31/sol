package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature

class BelowTemperatureTrigger(
    averageLowTemperature: Temperature = Temperature.celsius(0f),
) : LifecycleEventTrigger {

    private val averageLowC = averageLowTemperature.celsius().value

    override fun isTriggered(factors: LifecycleEventFactors): Boolean {
        val aboveFreezing =
            factors.temperatureHistory30Days.indexOfFirst { it.start.celsius().value > averageLowC }
        val belowFreezing =
            factors.temperatureHistory30Days.indexOfFirst { it.start.celsius().value <= averageLowC }
        // Drops below freezing
        return aboveFreezing != -1 && belowFreezing > aboveFreezing
    }

}