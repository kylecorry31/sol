package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.ecology.LifecycleEventFactor
import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration

class AboveDayLengthTriggerTest {

    @Test
    fun triggeredWhenDayLengthRisesAboveThreshold() {
        val trigger = AboveDayLengthTrigger(Duration.ofHours(12))
        val history = listOf(
            Duration.ofHours(11),
            Duration.ofHours(13)
        )
        val factors = factors(history)
        assertTrue(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWhenAlwaysAboveThreshold() {
        val trigger = AboveDayLengthTrigger(Duration.ofHours(12))
        val history = listOf(
            Duration.ofHours(12),
            Duration.ofHours(13)
        )
        val factors = factors(history)
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWhenAlwaysBelowThreshold() {
        val trigger = AboveDayLengthTrigger(Duration.ofHours(12))
        val history = listOf(
            Duration.ofHours(10),
            Duration.ofHours(11)
        )
        val factors = factors(history)
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWhenDayLengthDropsBelowAfterBeingAbove() {
        val trigger = AboveDayLengthTrigger(Duration.ofHours(12))
        val history = listOf(
            Duration.ofHours(13),
            Duration.ofHours(11)
        )
        val factors = factors(history)
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWithEmptyHistory() {
        val trigger = AboveDayLengthTrigger(Duration.ofHours(12))
        val factors = factors(emptyList())
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun triggeredWhenReachingThresholdFromBelow() {
        val trigger = AboveDayLengthTrigger(Duration.ofHours(12))
        val history = listOf(
            Duration.ofHours(11),
            Duration.ofHours(12)
        )
        val factors = factors(history)
        assertTrue(trigger.isTriggered(factors))
    }

    private fun factors(history: List<Duration>): LifecycleEventFactors {
        return LifecycleEventFactors(
            cumulativeGrowingDegreeDays = LifecycleEventFactor(0f, emptyList()),
            lengthOfDay = LifecycleEventFactor(Duration.ofHours(12), history),
            temperature = LifecycleEventFactor(
                Range(Temperature.celsius(0f), Temperature.celsius(0f)),
                emptyList()
            )
        )
    }
}
