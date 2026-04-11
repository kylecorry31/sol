package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class CumulativeGrowingDegreeDaysTriggerTest {

    @Test
    fun triggersWhenCumulativeGddReachesMinimum() {
        val trigger = CumulativeGrowingDegreeDaysTrigger(10f)

        assertFalse(trigger.isTriggered(factors(0f, 10f)))
        assertTrue(trigger.isTriggered(factors(0f, 10f)))
    }

    @Test
    fun resetClearsAccumulatedGdd() {
        val trigger = CumulativeGrowingDegreeDaysTrigger(8f)

        assertFalse(trigger.isTriggered(factors(0f, 10f)))
        trigger.reset()

        assertFalse(trigger.isTriggered(factors(0f, 10f)))
    }

    @Test
    fun zeroGddDayResetsAccumulationByDefault() {
        val trigger = CumulativeGrowingDegreeDaysTrigger(9f)

        assertFalse(trigger.isTriggered(factors(0f, 10f)))
        assertFalse(trigger.isTriggered(factors(0f, 0f)))
        assertFalse(trigger.isTriggered(factors(0f, 10f)))
    }

    @Test
    fun zeroGddResetHonorsZeroCountBeforeReset() {
        val trigger = CumulativeGrowingDegreeDaysTrigger(9f, zeroCountBeforeReset = 2)

        assertFalse(trigger.isTriggered(factors(0f, 10f)))
        assertFalse(trigger.isTriggered(factors(0f, 0f)))
        assertTrue(trigger.isTriggered(factors(0f, 10f)))
    }

    private fun factors(lowC: Float, highC: Float): LifecycleEventFactors {
        return LifecycleEventFactors(
            lengthOfDay = Duration.ofHours(12),
            temperature = Range(Temperature.celsius(lowC), Temperature.celsius(highC)),
            date = LocalDate.now()
        )
    }
}
