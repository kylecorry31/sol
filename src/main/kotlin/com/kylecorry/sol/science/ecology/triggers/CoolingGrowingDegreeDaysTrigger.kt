package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.TemperatureUnits

class CoolingGrowingDegreeDaysTrigger(
    minimum: Float,
    units: TemperatureUnits = TemperatureUnits.Celsius,
    private val days: Int = 30
) : LifecycleEventTrigger {

    private val minC = if (units == TemperatureUnits.Celsius) {
        minimum
    } else {
        minimum * 5 / 9f
    }

    override fun isTriggered(factors: LifecycleEventFactors): Boolean {
        // Get the difference between each cumulative GDD
        val gdd = mutableListOf<Float>()
        for (i in 1 until factors.cumulativeGrowingDegreeDayHistory30Days.size) {
            gdd.add(
                factors.cumulativeGrowingDegreeDayHistory30Days[i] - factors.cumulativeGrowingDegreeDayHistory30Days[i - 1]
            )
        }

        val cumulative = gdd.takeLast(days).sum()
        return cumulative <= minC
    }
}