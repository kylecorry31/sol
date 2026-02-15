package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.ecology.LifecycleEventFactor
import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Duration

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


    private fun createTrigger(value: Boolean): LifecycleEventTrigger {
        return object : LifecycleEventTrigger {
            override fun isTriggered(factors: LifecycleEventFactors): Boolean = value
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
