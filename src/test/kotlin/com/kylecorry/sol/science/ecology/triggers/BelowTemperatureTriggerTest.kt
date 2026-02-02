package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BelowTemperatureTriggerTest {

    @Test
    fun triggeredWhenTemperatureDropsBelowThreshold() {
        val trigger = BelowTemperatureTrigger(Temperature.celsius(0f))
        val history = listOf(
            Range(Temperature.celsius(5f), Temperature.celsius(15f)),
            Range(Temperature.celsius(-2f), Temperature.celsius(8f))
        )
        val factors = factors(history)
        assertTrue(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWhenAlwaysBelowThreshold() {
        val trigger = BelowTemperatureTrigger(Temperature.celsius(0f))
        val history = listOf(
            Range(Temperature.celsius(-5f), Temperature.celsius(5f)),
            Range(Temperature.celsius(-10f), Temperature.celsius(2f))
        )
        val factors = factors(history)
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWhenAlwaysAboveThreshold() {
        val trigger = BelowTemperatureTrigger(Temperature.celsius(0f))
        val history = listOf(
            Range(Temperature.celsius(5f), Temperature.celsius(15f)),
            Range(Temperature.celsius(8f), Temperature.celsius(20f))
        )
        val factors = factors(history)
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWhenTemperatureRisesAboveAfterBeingBelow() {
        val trigger = BelowTemperatureTrigger(Temperature.celsius(0f))
        val history = listOf(
            Range(Temperature.celsius(-5f), Temperature.celsius(5f)),
            Range(Temperature.celsius(5f), Temperature.celsius(15f))
        )
        val factors = factors(history)
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWithEmptyHistory() {
        val trigger = BelowTemperatureTrigger(Temperature.celsius(0f))
        val factors = factors(emptyList())
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun worksWithCustomThreshold() {
        val trigger = BelowTemperatureTrigger(Temperature.celsius(10f))
        val history = listOf(
            Range(Temperature.celsius(15f), Temperature.celsius(25f)),
            Range(Temperature.celsius(5f), Temperature.celsius(12f))
        )
        val factors = factors(history)
        assertTrue(trigger.isTriggered(factors))
    }

    private fun factors(history: List<Range<Temperature>>): LifecycleEventFactors {
        return LifecycleEventFactors(0f, history, emptyList())
    }
}
