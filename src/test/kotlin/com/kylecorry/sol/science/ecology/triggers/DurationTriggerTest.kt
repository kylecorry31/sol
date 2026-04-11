package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class DurationTriggerTest {

    @Test
    fun triggersAfterDurationFromBaseTriggerDate() {
        val baseTrigger = DateThresholdTrigger(LocalDate.of(2024, 1, 2))
        val trigger = DurationTrigger(baseTrigger, Duration.ofDays(2))

        assertFalse(trigger.isTriggered(factors(LocalDate.of(2024, 1, 1))))
        assertFalse(trigger.isTriggered(factors(LocalDate.of(2024, 1, 2))))
        assertFalse(trigger.isTriggered(factors(LocalDate.of(2024, 1, 3))))
        assertTrue(trigger.isTriggered(factors(LocalDate.of(2024, 1, 4))))
    }

    @Test
    fun resetWhenBaseNotTriggeredClearsPendingTriggerDate() {
        val baseTrigger = OneDayTrigger(LocalDate.of(2024, 1, 2))
        val trigger = DurationTrigger(baseTrigger, Duration.ofDays(2), resetWhenBaseNotTriggered = true)

        assertFalse(trigger.isTriggered(factors(LocalDate.of(2024, 1, 2))))
        assertFalse(trigger.isTriggered(factors(LocalDate.of(2024, 1, 3))))
        assertFalse(trigger.isTriggered(factors(LocalDate.of(2024, 1, 4))))
    }

    @Test
    fun resetCallsResetOnBaseTrigger() {
        val baseTrigger = ResetTrackingTrigger()
        val trigger = DurationTrigger(baseTrigger, Duration.ofDays(1))

        trigger.reset()

        assertEquals(1, baseTrigger.resetCount)
    }

    private fun factors(date: LocalDate): LifecycleEventFactors {
        return LifecycleEventFactors(
            lengthOfDay = Duration.ofHours(12),
            temperature = Range(Temperature.celsius(0f), Temperature.celsius(0f)),
            date = date
        )
    }

    private class DateThresholdTrigger(private val startDate: LocalDate) : LifecycleEventTrigger {
        override fun isTriggered(factors: LifecycleEventFactors): Boolean {
            return factors.date >= startDate
        }
    }

    private class OneDayTrigger(private val triggerDate: LocalDate) : LifecycleEventTrigger {
        override fun isTriggered(factors: LifecycleEventFactors): Boolean {
            return factors.date == triggerDate
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
}
