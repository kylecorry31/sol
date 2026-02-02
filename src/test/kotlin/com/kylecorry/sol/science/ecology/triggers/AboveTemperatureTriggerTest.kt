package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AboveTemperatureTriggerTest {

    @Test
    fun triggeredWhenTemperatureRisesAboveThreshold() {
        val trigger = AboveTemperatureTrigger(Temperature.celsius(10f))
        val history = listOf(
            Range(Temperature.celsius(-5f), Temperature.celsius(5f)),
            Range(Temperature.celsius(0f), Temperature.celsius(15f))
        )
        val factors = factors(history)
        assertTrue(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWhenAlwaysAboveThreshold() {
        val trigger = AboveTemperatureTrigger(Temperature.celsius(10f))
        val history = listOf(
            Range(Temperature.celsius(5f), Temperature.celsius(15f)),
            Range(Temperature.celsius(8f), Temperature.celsius(20f))
        )
        val factors = factors(history)
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWhenAlwaysBelowThreshold() {
        val trigger = AboveTemperatureTrigger(Temperature.celsius(10f))
        val history = listOf(
            Range(Temperature.celsius(-5f), Temperature.celsius(5f)),
            Range(Temperature.celsius(-2f), Temperature.celsius(8f))
        )
        val factors = factors(history)
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWhenTemperatureDropsBelowAfterBeingAbove() {
        val trigger = AboveTemperatureTrigger(Temperature.celsius(10f))
        val history = listOf(
            Range(Temperature.celsius(5f), Temperature.celsius(15f)),
            Range(Temperature.celsius(-5f), Temperature.celsius(5f))
        )
        val factors = factors(history)
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWithEmptyHistory() {
        val trigger = AboveTemperatureTrigger(Temperature.celsius(10f))
        val factors = factors(emptyList())
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun workWithFahrenheitThreshold() {
        val trigger = AboveTemperatureTrigger(Temperature.fahrenheit(50f))
        val history = listOf(
            Range(Temperature.celsius(-5f), Temperature.celsius(5f)),
            Range(Temperature.celsius(0f), Temperature.celsius(15f))
        )
        val factors = factors(history)
        assertTrue(trigger.isTriggered(factors))
    }

    private fun factors(history: List<Range<Temperature>>): LifecycleEventFactors {
        return LifecycleEventFactors(0f, history, emptyList())
    }
}
