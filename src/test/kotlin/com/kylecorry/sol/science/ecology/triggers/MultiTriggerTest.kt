package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.ecology.LifecycleEventFactor
import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration

class MultiTriggerTest {

    @Test
    fun triggeredWhenAllTriggersPass() {
        val trigger = MultiTrigger(alwaysTrue(), alwaysTrue())
        assertTrue(trigger.isTriggered(factors()))
    }

    @Test
    fun notTriggeredWhenOneTriggersIsFalse() {
        val trigger = MultiTrigger(alwaysTrue(), alwaysFalse())
        assertFalse(trigger.isTriggered(factors()))
    }

    @Test
    fun notTriggeredWhenAllTriggersAreFalse() {
        val trigger = MultiTrigger(alwaysFalse(), alwaysFalse())
        assertFalse(trigger.isTriggered(factors()))
    }

    @Test
    fun triggeredWithSingleTriggerThatPasses() {
        val trigger = MultiTrigger(alwaysTrue())
        assertTrue(trigger.isTriggered(factors()))
    }

    @Test
    fun notTriggeredWithSingleTriggerThatFails() {
        val trigger = MultiTrigger(alwaysFalse())
        assertFalse(trigger.isTriggered(factors()))
    }

    private fun alwaysTrue(): LifecycleEventTrigger {
        return object : LifecycleEventTrigger {
            override fun isTriggered(factors: LifecycleEventFactors): Boolean = true
        }
    }

    private fun alwaysFalse(): LifecycleEventTrigger {
        return object : LifecycleEventTrigger {
            override fun isTriggered(factors: LifecycleEventFactors): Boolean = false
        }
    }

    private fun factors(): LifecycleEventFactors {
        return LifecycleEventFactors(
            cumulativeGrowingDegreeDays = LifecycleEventFactor(0f, emptyList()),
            lengthOfDay = LifecycleEventFactor(Duration.ofHours(12), emptyList()),
            temperature = LifecycleEventFactor(
                Range(Temperature.celsius(0f), Temperature.celsius(0f)),
                emptyList()
            )
        )
    }
}
