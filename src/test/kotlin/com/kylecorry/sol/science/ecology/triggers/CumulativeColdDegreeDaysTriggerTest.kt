package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class CumulativeColdDegreeDaysTriggerTest {

    @Test
    fun triggersWhenCumulativeCddReachesMinimum() {
        val trigger = CumulativeColdDegreeDaysTrigger(10f, baseTemperature = Temperature.celsius(10f))

        assertFalse(trigger.isTriggered(factors(0f, 10f)))
        assertTrue(trigger.isTriggered(factors(0f, 10f)))
    }

    @Test
    fun resetClearsAccumulatedCdd() {
        val trigger = CumulativeColdDegreeDaysTrigger(8f, baseTemperature = Temperature.celsius(10f))

        assertFalse(trigger.isTriggered(factors(0f, 10f)))
        trigger.reset()

        assertFalse(trigger.isTriggered(factors(0f, 10f)))
    }

    @Test
    fun zeroCddDayResetsAccumulationByDefault() {
        val trigger = CumulativeColdDegreeDaysTrigger(9f, baseTemperature = Temperature.celsius(10f))

        assertFalse(trigger.isTriggered(factors(0f, 10f)))
        assertFalse(trigger.isTriggered(factors(10f, 10f)))
        assertFalse(trigger.isTriggered(factors(0f, 10f)))
    }

    @Test
    fun zeroCddResetHonorsZeroCountBeforeReset() {
        val trigger = CumulativeColdDegreeDaysTrigger(
            9f,
            baseTemperature = Temperature.celsius(10f),
            zeroCountBeforeReset = 2
        )

        assertFalse(trigger.isTriggered(factors(0f, 10f)))
        assertFalse(trigger.isTriggered(factors(10f, 10f)))
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
