package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.science.ecology.Ecology
import com.kylecorry.sol.science.ecology.GrowingDegreeDaysCalculationType
import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature
import com.kylecorry.sol.units.TemperatureUnits

class CumulativeGrowingDegreeDaysTrigger(
    minimum: Float,
    units: TemperatureUnits = TemperatureUnits.Celsius,
    private val baseTemperature: Temperature = Temperature.zero,
    private val limit: Float = Float.MAX_VALUE,
    private val calculationType: GrowingDegreeDaysCalculationType = GrowingDegreeDaysCalculationType.MinMax,
    private val zeroCountBeforeReset: Int = 1
) : LifecycleEventTrigger {

    private var total = 0f
    private var zeroCount = 0
    private val minimumC = if (units == TemperatureUnits.Celsius) {
        minimum
    } else {
        minimum * 5 / 9f
    }

    override fun isTriggered(factors: LifecycleEventFactors): Boolean {
        val gdd = Ecology.getGrowingDegreeDays(factors.temperature, baseTemperature, limit, calculationType)
        if (Arithmetic.isZero(gdd)) {
            zeroCount++
        } else {
            zeroCount = 0
        }
        if (zeroCount >= zeroCountBeforeReset) {
            reset()
        }
        total += gdd
        return total >= minimumC
    }

    override fun reset() {
        total = 0f
    }
}