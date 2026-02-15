package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature

class AboveTemperatureTrigger(
    averageHighTemperature: Temperature = Temperature.celsius(0f),
) : LifecycleEventTrigger {

    private val averageHighC = averageHighTemperature.celsius().value

    override fun isTriggered(factors: LifecycleEventFactors): Boolean {
        return factors.temperature.current.end.celsius().value > averageHighC
    }

}