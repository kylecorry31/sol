package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Duration
import java.time.LocalDate

class TemperatureTriggerTest {

    @ParameterizedTest
    @CsvSource(
        "10, false",
        "11, true",
        "9, false"
    )
    fun aboveTriggeredWhenTemperatureIsAboveThreshold(temperature: Float, isTriggered: Boolean) {
        val trigger = TemperatureTrigger(Temperature.celsius(10f), above = true)
        val factors = factors(low = 0f, high = temperature)
        assertEquals(isTriggered, trigger.isTriggered(factors))
    }

    @ParameterizedTest
    @CsvSource(
        "0, false",
        "-1, true",
        "1, false"
    )
    fun belowTriggeredWhenTemperatureIsBelowThreshold(temperature: Float, isTriggered: Boolean) {
        val trigger = TemperatureTrigger(Temperature.celsius(0f), above = false)
        val factors = factors(low = temperature, high = 0f)
        assertEquals(isTriggered, trigger.isTriggered(factors))
    }

    @ParameterizedTest
    @CsvSource(
        "true, High, true",
        "true, Low, false",
        "true, Average, false",
        "false, High, false",
        "false, Low, true",
        "false, Average, false"
    )
    fun triggeredUsesConfiguredTemperatureType(
        above: Boolean,
        triggerType: TemperatureTriggerType,
        isTriggered: Boolean
    ) {
        val trigger = TemperatureTrigger(Temperature.celsius(10f), above, triggerType)
        val factors = factors(low = 0f, high = 20f)
        assertEquals(isTriggered, trigger.isTriggered(factors))
    }

    private fun factors(low: Float, high: Float): LifecycleEventFactors {
        return LifecycleEventFactors(
            lengthOfDay = Duration.ofHours(12),
            temperature = Range(Temperature.celsius(low), Temperature.celsius(high)),
            date = LocalDate.now()
        )
    }
}
