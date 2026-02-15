package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.ecology.LifecycleEventFactor
import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Duration

class BelowTemperatureTriggerTest {

    @ParameterizedTest
    @CsvSource(
        "0, false",
        "-1, true",
        "1, false"
    )
    fun triggeredWhenTemperatureIsBelowThreshold(temperature: Float, isTriggered: Boolean) {
        val trigger = BelowTemperatureTrigger(Temperature.celsius(0f))
        val factors = factors(temperature)
        assertEquals(isTriggered, trigger.isTriggered(factors))
    }

    private fun factors(temperature: Float): LifecycleEventFactors {
        return LifecycleEventFactors(
            cumulativeGrowingDegreeDays = LifecycleEventFactor(0f, emptyList()),
            lengthOfDay = LifecycleEventFactor(Duration.ofHours(12), emptyList()),
            temperature = LifecycleEventFactor(
                Range(Temperature.celsius(temperature), Temperature.celsius(0f)),
                emptyList()
            )
        )
    }
}
