package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactor
import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.math.Range
import com.kylecorry.sol.units.Temperature
import com.kylecorry.sol.units.TemperatureUnits
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration

class MinimumGrowingDegreeDaysTriggerTest {

    @Test
    fun triggeredWhenGDDAtMinimum() {
        val trigger = MinimumGrowingDegreeDaysTrigger(100f)
        val factors = factors(cumulativeGDD = 100f)
        assertTrue(trigger.isTriggered(factors))
    }

    @Test
    fun triggeredWhenGDDAboveMinimum() {
        val trigger = MinimumGrowingDegreeDaysTrigger(100f)
        val factors = factors(cumulativeGDD = 150f)
        assertTrue(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWhenGDDBelowMinimum() {
        val trigger = MinimumGrowingDegreeDaysTrigger(100f)
        val factors = factors(cumulativeGDD = 99f)
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun convertsFahrenheitToCelsius() {
        // 180F GDD = 100C GDD
        val trigger = MinimumGrowingDegreeDaysTrigger(180f, TemperatureUnits.Fahrenheit)
        val factors = factors(cumulativeGDD = 100f)
        assertTrue(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWhenBelowFahrenheitMinimum() {
        val trigger = MinimumGrowingDegreeDaysTrigger(180f, TemperatureUnits.Fahrenheit)
        val factors = factors(cumulativeGDD = 99f)
        assertFalse(trigger.isTriggered(factors))
    }

    private fun factors(cumulativeGDD: Float): LifecycleEventFactors {
        return LifecycleEventFactors(
            cumulativeGrowingDegreeDays = LifecycleEventFactor(cumulativeGDD, listOf(cumulativeGDD)),
            lengthOfDay = LifecycleEventFactor(Duration.ofHours(12), emptyList()),
            temperature = LifecycleEventFactor(
                Range(Temperature.celsius(0f), Temperature.celsius(0f)),
                emptyList()
            )
        )
    }
}
