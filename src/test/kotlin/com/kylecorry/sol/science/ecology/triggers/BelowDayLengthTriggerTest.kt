package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.ecology.LifecycleEventFactor
import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Duration

class BelowDayLengthTriggerTest {

    @ParameterizedTest
    @CsvSource(
        "12, false",
        "11, true",
        "13, false"
    )
    fun triggeredWhenDayLengthIsBelowThreshold(dayLengthHours: Long, isTriggered: Boolean) {
        val trigger = BelowDayLengthTrigger(Duration.ofHours(12))
        val factors = factors(dayLengthHours)
        assertEquals(isTriggered, trigger.isTriggered(factors))
    }

    private fun factors(dayLengthHours: Long): LifecycleEventFactors {
        return LifecycleEventFactors(
            cumulativeGrowingDegreeDays = LifecycleEventFactor(0f, emptyList()),
            lengthOfDay = LifecycleEventFactor(Duration.ofHours(dayLengthHours), emptyList()),
            temperature = LifecycleEventFactor(
                Range(Temperature.celsius(0f), Temperature.celsius(0f)),
                emptyList()
            )
        )
    }
}
