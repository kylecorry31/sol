package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.TemperatureUnits

class MinimumGrowingDegreeDaysTrigger(
    minimum: Float,
    units: TemperatureUnits = TemperatureUnits.Celsius
) : LifecycleEventTrigger {

    private val minC = if (units == TemperatureUnits.Celsius) {
        minimum
    } else {
        minimum * 5 / 9f
    }

    override fun isTriggered(factors: LifecycleEventFactors): Boolean {
        return factors.cumulativeGrowingDegreeDays.current >= minC
    }
}