package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Duration
import java.time.LocalDate

class MultiTriggerTest {


    @ParameterizedTest
    @CsvSource(
        "true, true, true",
        "true, false, false",
        "false, true, false",
        "false, false, false",
    )
    fun triggersWithAnyFalse(trigger1: Boolean, trigger2: Boolean, expected: Boolean) {
        val trigger = MultiTrigger(createTrigger(trigger1), createTrigger(trigger2))
        assertEquals(expected, trigger.isTriggered(factors()))
    }

    @ParameterizedTest
    @CsvSource(
        "true, true, true",
        "true, false, true",
        "false, true, true",
        "false, false, false",
    )
    fun triggersWithAnyTrue(trigger1: Boolean, trigger2: Boolean, expected: Boolean) {
        val trigger = MultiTrigger(createTrigger(trigger1), createTrigger(trigger2), any = true)
        assertEquals(expected, trigger.isTriggered(factors()))
    }

    @Test
    fun resetCallsResetOnAllTriggers() {
        val trigger1 = ResetTrackingTrigger()
        val trigger2 = ResetTrackingTrigger()
        val trigger3 = ResetTrackingTrigger()
        val trigger = MultiTrigger(trigger1, trigger2, trigger3)

        trigger.reset()

        assertEquals(1, trigger1.resetCount)
        assertEquals(1, trigger2.resetCount)
        assertEquals(1, trigger3.resetCount)
    }

    private fun createTrigger(value: Boolean): LifecycleEventTrigger {
        return object : LifecycleEventTrigger {
            override fun isTriggered(factors: LifecycleEventFactors): Boolean = value
        }
    }

    private class ResetTrackingTrigger : LifecycleEventTrigger {
        var resetCount = 0
            private set

        override fun isTriggered(factors: LifecycleEventFactors): Boolean = false

        override fun reset() {
            resetCount++
        }
    }

    private fun factors(): LifecycleEventFactors {
        return LifecycleEventFactors(
            lengthOfDay = Duration.ofHours(12),
            temperature = Range(Temperature.celsius(0f), Temperature.celsius(0f)),
            date = LocalDate.now()
        )
    }
}
