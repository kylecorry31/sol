package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration

class BelowDayLengthTriggerTest {

    @Test
    fun triggeredWhenDayLengthDropsBelowThreshold() {
        val trigger = BelowDayLengthTrigger(Duration.ofHours(12))
        val history = listOf(
            Duration.ofHours(13),
            Duration.ofHours(11)
        )
        val factors = factors(history)
        assertTrue(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWhenAlwaysBelowThreshold() {
        val trigger = BelowDayLengthTrigger(Duration.ofHours(12))
        val history = listOf(
            Duration.ofHours(11),
            Duration.ofHours(12)
        )
        val factors = factors(history)
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWhenAlwaysAboveThreshold() {
        val trigger = BelowDayLengthTrigger(Duration.ofHours(12))
        val history = listOf(
            Duration.ofHours(13),
            Duration.ofHours(14)
        )
        val factors = factors(history)
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWhenDayLengthRisesAboveAfterBeingBelow() {
        val trigger = BelowDayLengthTrigger(Duration.ofHours(12))
        val history = listOf(
            Duration.ofHours(11),
            Duration.ofHours(13)
        )
        val factors = factors(history)
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun notTriggeredWithEmptyHistory() {
        val trigger = BelowDayLengthTrigger(Duration.ofHours(12))
        val factors = factors(emptyList())
        assertFalse(trigger.isTriggered(factors))
    }

    @Test
    fun triggeredWhenReachingThresholdFromAbove() {
        val trigger = BelowDayLengthTrigger(Duration.ofHours(12))
        val history = listOf(
            Duration.ofHours(13),
            Duration.ofHours(12)
        )
        val factors = factors(history)
        assertTrue(trigger.isTriggered(factors))
    }

    private fun factors(history: List<Duration>): LifecycleEventFactors {
        return LifecycleEventFactors(0f, Duration.ofHours(12), emptyList(), emptyList(), history)
    }
}
