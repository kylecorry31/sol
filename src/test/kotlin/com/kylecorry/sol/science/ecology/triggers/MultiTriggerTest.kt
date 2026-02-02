package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
        return LifecycleEventFactors(0f, emptyList(), emptyList())
    }
}
