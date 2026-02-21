package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature
import com.kylecorry.sol.units.average

class TemperatureTrigger(
    threshold: Temperature = Temperature.celsius(0f),
    private val above: Boolean = true,
    private val triggerType: TemperatureTriggerType = if (above) TemperatureTriggerType.High else TemperatureTriggerType.Low
) : LifecycleEventTrigger {

    private val thresholdC = threshold.celsius().value

    override fun isTriggered(factors: LifecycleEventFactors): Boolean {
        val temperature = when (triggerType) {
            TemperatureTriggerType.High -> factors.temperature.end
            TemperatureTriggerType.Low -> factors.temperature.start
            TemperatureTriggerType.Average -> factors.temperature.average()
        }

        return if (above) {
            temperature.celsius().value > thresholdC
        } else {
            temperature.celsius().value < thresholdC
        }
    }

}
