package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.TemperatureUnits
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CoolingGrowingDegreeDaysTriggerTest {

    @Test
    fun triggeredWhenCoolingBelowMinimum() {
        val trigger = CoolingGrowingDegreeDaysTrigger(5f, days = 3)
        // Daily GDD: 2, 1, 1 = 4, which is <= 5
        val cumulativeHistory = listOf(0f, 2f, 3f, 4f)
        val factors = factors(cumulativeHistory)
        assertTrue(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWhenGDDAboveMinimum() {
        val trigger = CoolingGrowingDegreeDaysTrigger(5f, days = 3)
        // Daily GDD: 3, 3, 3 = 9, which is > 5
        val cumulativeHistory = listOf(0f, 3f, 6f, 9f)
        val factors = factors(cumulativeHistory)
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun triggeredWhenExactlyAtMinimum() {
        val trigger = CoolingGrowingDegreeDaysTrigger(6f, days = 3)
        // Daily GDD: 2, 2, 2 = 6, which is <= 6
        val cumulativeHistory = listOf(0f, 2f, 4f, 6f)
        val factors = factors(cumulativeHistory)
        assertTrue(trigger.isTriggered(factors))
    }

    @Test
    fun usesOnlyLastNDays() {
        val trigger = CoolingGrowingDegreeDaysTrigger(5f, days = 2)
        // Daily GDD: 10, 1, 1 -> last 2 days = 2, which is <= 5
        val cumulativeHistory = listOf(0f, 10f, 11f, 12f)
        val factors = factors(cumulativeHistory)
        assertTrue(trigger.isTriggered(factors))
    }

    @Test
    fun convertsFahrenheitToCelsius() {
        // 9F GDD = 5C GDD
        val trigger = CoolingGrowingDegreeDaysTrigger(9f, TemperatureUnits.Fahrenheit, days = 3)
        // Daily GDD: 2, 1, 1 = 4, which is <= 5
        val cumulativeHistory = listOf(0f, 2f, 3f, 4f)
        val factors = factors(cumulativeHistory)
        assertTrue(trigger.isTriggered(factors))
    }

    @Test
    fun handlesEmptyHistory() {
        val trigger = CoolingGrowingDegreeDaysTrigger(5f, days = 3)
        val factors = factors(emptyList())
        assertTrue(trigger.isTriggered(factors))
    }

    private fun factors(cumulativeHistory: List<Float>): LifecycleEventFactors {
        return LifecycleEventFactors(0f, emptyList(), cumulativeHistory)
    }
}
