package com.kylecorry.sol.science.ecology.triggers

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.ecology.LifecycleEventFactors
import com.kylecorry.sol.units.Temperature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Duration
import java.time.LocalDate

class PhotoperiodTriggerTest {

    @ParameterizedTest
    @CsvSource(
        "12, false",
        "13, true",
        "11, false"
    )
    fun aboveTriggeredWhenDayLengthIsAboveThreshold(dayLengthHours: Long, isTriggered: Boolean) {
        val trigger = PhotoperiodTrigger(Duration.ofHours(12), above = true)
        val factors = factors(dayLengthHours)
        assertEquals(isTriggered, trigger.isTriggered(factors))
    }

    @ParameterizedTest
    @CsvSource(
        "12, false",
        "11, true",
        "13, false"
    )
    fun belowTriggeredWhenDayLengthIsBelowThreshold(dayLengthHours: Long, isTriggered: Boolean) {
        val trigger = PhotoperiodTrigger(Duration.ofHours(12), above = false)
        val factors = factors(dayLengthHours)
        assertEquals(isTriggered, trigger.isTriggered(factors))
    }

    private fun factors(dayLengthHours: Long): LifecycleEventFactors {
        return LifecycleEventFactors(
            lengthOfDay = Duration.ofHours(dayLengthHours),
            temperature = Range(Temperature.celsius(0f), Temperature.celsius(0f)),
            date = LocalDate.now()
        )
    }
}
