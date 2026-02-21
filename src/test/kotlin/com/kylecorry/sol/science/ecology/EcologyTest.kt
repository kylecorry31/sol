package com.kylecorry.sol.science.ecology

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.trigonometry.SineWave
import com.kylecorry.sol.science.ecology.triggers.CumulativeGrowingDegreeDaysTrigger
import com.kylecorry.sol.science.ecology.triggers.LifecycleEventTrigger
import com.kylecorry.sol.science.ecology.triggers.PhotoperiodTrigger
import com.kylecorry.sol.science.ecology.triggers.TemperatureTrigger
import com.kylecorry.sol.units.Temperature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import kotlin.math.PI

class EcologyTest {

    // getGrowingDegreeDays

    @Test
    fun gddIsAverageMinusBase() {
        val gdd = Ecology.getGrowingDegreeDays(
            Range(Temperature.celsius(10f), Temperature.celsius(20f)),
            Temperature.celsius(10f)
        )
        // (20 + 10) / 2 - 10 = 5
        assertEquals(5f, gdd)
    }

    @Test
    fun gddIsZeroWhenAverageBelowBase() {
        val gdd = Ecology.getGrowingDegreeDays(
            Range(Temperature.celsius(0f), Temperature.celsius(5f)),
            Temperature.celsius(10f)
        )
        assertEquals(0f, gdd)
    }

    @Test
    fun gddCapsMaxTemperature() {
        val gdd = Ecology.getGrowingDegreeDays(
            Range(Temperature.celsius(10f), Temperature.celsius(40f)),
            Temperature.celsius(10f),
            limit = 30f
        )
        // (30 + 10) / 2 - 10 = 10
        assertEquals(10f, gdd)
    }

    @Test
    fun gddBaseMaxUsesBaseWhenMinBelowBase() {
        val gdd = Ecology.getGrowingDegreeDays(
            Range(Temperature.celsius(0f), Temperature.celsius(20f)),
            Temperature.celsius(10f),
            calculationType = GrowingDegreeDaysCalculationType.BaseMax
        )
        // min is 0 < base 10, so min becomes 10: (20 + 10) / 2 - 10 = 5
        assertEquals(5f, gdd)
    }

    @Test
    fun gddBaseMaxKeepsMinWhenAboveBase() {
        val gdd = Ecology.getGrowingDegreeDays(
            Range(Temperature.celsius(15f), Temperature.celsius(25f)),
            Temperature.celsius(10f),
            calculationType = GrowingDegreeDaysCalculationType.BaseMax
        )
        // min 15 >= base 10, so: (25 + 15) / 2 - 10 = 10
        assertEquals(10f, gdd)
    }

    @Test
    fun gddMinMaxDoesNotReplaceMinBelowBase() {
        val gdd = Ecology.getGrowingDegreeDays(
            Range(Temperature.celsius(0f), Temperature.celsius(20f)),
            Temperature.celsius(10f),
            calculationType = GrowingDegreeDaysCalculationType.MinMax
        )
        // (20 + 0) / 2 - 10 = 0
        assertEquals(0f, gdd)
    }

    @Test
    fun gddWorkWithFahrenheit() {
        val gdd = Ecology.getGrowingDegreeDays(
            Range(Temperature.fahrenheit(50f), Temperature.fahrenheit(68f)),
            Temperature.celsius(10f)
        )
        // 50F = 10C, 68F = 20C: (20 + 10) / 2 - 10 = 5
        assertEquals(5f, gdd, 0.01f)
    }

    // getColdDegreeDays

    @Test
    fun cddIsBaseMinusAverage() {
        val cdd = Ecology.getColdDegreeDays(
            Range(Temperature.celsius(0f), Temperature.celsius(10f)),
            Temperature.celsius(10f)
        )
        // 10 - ((10 + 0) / 2) = 5
        assertEquals(5f, cdd)
    }

    @Test
    fun cddIsZeroWhenAverageAboveBase() {
        val cdd = Ecology.getColdDegreeDays(
            Range(Temperature.celsius(10f), Temperature.celsius(20f)),
            Temperature.celsius(10f)
        )
        assertEquals(0f, cdd)
    }

    @Test
    fun cddCapsMaxTemperature() {
        val cdd = Ecology.getColdDegreeDays(
            Range(Temperature.celsius(0f), Temperature.celsius(30f)),
            Temperature.celsius(20f),
            limit = 10f
        )
        // 20 - ((10 + 0) / 2) = 15
        assertEquals(15f, cdd)
    }

    @Test
    fun cddWorksWithFahrenheit() {
        val cdd = Ecology.getColdDegreeDays(
            Range(Temperature.fahrenheit(32f), Temperature.fahrenheit(50f)),
            Temperature.celsius(10f)
        )
        // 32F = 0C, 50F = 10C: 10 - ((10 + 0) / 2) = 5
        assertEquals(5f, cdd, 0.01f)
    }

    // getCumulativeGrowingDegreeDays

    @Test
    fun cumulativeGDDAccumulatesOverDates() {
        val base = Temperature.celsius(0f)
        // Use a date right after the coldest day so accumulation starts from the query dates
        val coldestDate = LocalDate.of(2023, 12, 31)
        val startDate = LocalDate.of(2024, 1, 1)
        val dates = listOf(startDate, startDate.plusDays(1), startDate.plusDays(2))

        val result = Ecology.getCumulativeGrowingDegreeDays(
            dates,
            base,
            temperatureProvider = {
                if (it == coldestDate) {
                    Range(Temperature.celsius(-50f), Temperature.celsius(-40f))
                } else {
                    Range(Temperature.celsius(0f), Temperature.celsius(10f))
                }
            }
        )

        assertEquals(3, result.size)
        // Each day: (10 + 0) / 2 - 0 = 5 GDD, coldest day contributes 0
        assertEquals(startDate, result[0].first)
        assertEquals(5f, result[0].second)
        assertEquals(10f, result[1].second)
        assertEquals(15f, result[2].second)
    }

    @Test
    fun cumulativeGDDReturnsEmptyForEmptyDates() {
        val result = Ecology.getCumulativeGrowingDegreeDays(
            emptyList(),
            Temperature.celsius(0f),
            ::mockTemperatureProvider
        )
        assertEquals(emptyList<Pair<LocalDate, Float>>(), result)
    }

    @Test
    fun cumulativeGDDResetsAnnually() {
        val base = Temperature.celsius(0f)
        // Two dates more than a year apart - the coldest day in the year prior should reset
        val date1 = LocalDate.of(2024, 7, 1)
        val date2 = LocalDate.of(2025, 7, 1)

        val result = Ecology.getCumulativeGrowingDegreeDays(
            listOf(date1, date2),
            base,
            { Range(Temperature.celsius(0f), Temperature.celsius(10f)) }
        )

        assertEquals(2, result.size)
        // Both should have accumulated GDD, but date2 should have reset at the annual boundary
        // so they should be roughly equal (same constant temperature)
        assertEquals(result[0].second, result[1].second, 5.1f)
    }

    // getLifecycleEventDates
    @Test
    fun getLifecycleEventDates() {
        val events = listOf(
            // Hits the GDD threshold once
            LifecycleEvent("event1", CumulativeGrowingDegreeDaysTrigger(1000f)),
            // Hits the temperature threshold twice (carryover from previous year + once at end of year)
            LifecycleEvent("event2", TemperatureTrigger(Temperature.celsius(5f), above = false)),
            // Hits the day length threshold once
            LifecycleEvent("event3", PhotoperiodTrigger(Duration.ofHours(12), above = true)),
            // Never reached
            LifecycleEvent("event4", PhotoperiodTrigger(Duration.ofHours(100), above = true))
        )

        val start = LocalDate.of(2024, 1, 1)
        val end = LocalDate.of(2025, 1, 1)

        val result = Ecology.getLifecycleEventDates(
            events,
            Range(start, end),
            ::mockDayLengthProvider,
            ::mockTemperatureProvider
        )

        assertEquals(4, result.size)
        assertEquals(LocalDate.of(2024, 1, 1), result[0].first)
        assertEquals("event2", result[0].second.name)

        assertEquals(LocalDate.of(2024, 3, 28), result[1].first)
        assertEquals("event3", result[1].second.name)

        assertEquals(LocalDate.of(2024, 5, 19), result[2].first)
        assertEquals("event1", result[2].second.name)

        assertEquals(LocalDate.of(2024, 10, 1), result[3].first)
        assertEquals("event2", result[3].second.name)
    }

    @Test
    fun lifecycleEventReturnsEmptyForEmptyRange() {
        val event = LifecycleEvent("bloom", CumulativeGrowingDegreeDaysTrigger(10f))
        val events = listOf(event)

        val date = LocalDate.of(2024, 6, 1)

        val result = Ecology.getLifecycleEventDates(
            events,
            Range(date, date.minusDays(1)),
            { Duration.ofHours(12) },
            ::mockTemperatureProvider
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun lifecycleEventResetsTriggersBeforeEvaluation() {
        val callOrder = mutableListOf<String>()
        val trigger = object : LifecycleEventTrigger {
            override fun isTriggered(factors: LifecycleEventFactors): Boolean {
                callOrder.add("isTriggered")
                return false
            }

            override fun reset() {
                callOrder.add("reset")
            }
        }

        Ecology.getLifecycleEventDates(
            listOf(LifecycleEvent("event", trigger)),
            Range(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)),
            { Duration.ofHours(12) },
            ::mockTemperatureProvider
        )

        assertTrue(callOrder.isNotEmpty())
        assertEquals("reset", callOrder.first())
    }

    // getActivePeriodsForYear

    @Test
    fun activePeriodsReturnsEmptyForNoEvents() {
        val result = Ecology.getActivePeriodsForYear(
            2024,
            emptyList(),
            "start",
            "end"
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun activePeriodsReturnsJan1ToEndWhenOnlyEndEvent() {
        val trigger = CumulativeGrowingDegreeDaysTrigger(1f)
        val events = listOf(
            Pair(LocalDate.of(2024, 3, 15), LifecycleEvent("end", trigger))
        )

        val result = Ecology.getActivePeriodsForYear(2024, events, "start", "end")

        assertEquals(1, result.size)
        assertEquals(LocalDate.of(2024, 1, 1), result[0].start)
        assertEquals(LocalDate.of(2024, 3, 15), result[0].end)
    }

    @Test
    fun activePeriodsReturnsStartToDec31WhenOnlyStartEvent() {
        val trigger = CumulativeGrowingDegreeDaysTrigger(1f)
        val events = listOf(
            Pair(LocalDate.of(2024, 5, 20), LifecycleEvent("start", trigger))
        )

        val result = Ecology.getActivePeriodsForYear(2024, events, "start", "end")

        assertEquals(1, result.size)
        assertEquals(LocalDate.of(2024, 5, 20), result[0].start)
        assertEquals(LocalDate.of(2024, 12, 31), result[0].end)
    }

    @Test
    fun activePeriodsReturnsStartToEndWhenBothExist() {
        val trigger = CumulativeGrowingDegreeDaysTrigger(1f)
        val events = listOf(
            Pair(LocalDate.of(2024, 4, 1), LifecycleEvent("start", trigger)),
            Pair(LocalDate.of(2024, 9, 30), LifecycleEvent("end", trigger))
        )

        val result = Ecology.getActivePeriodsForYear(2024, events, "start", "end")

        assertEquals(1, result.size)
        assertEquals(LocalDate.of(2024, 4, 1), result[0].start)
        assertEquals(LocalDate.of(2024, 9, 30), result[0].end)
    }

    @Test
    fun activePeriodsReturnsMultiplePeriodsInYear() {
        val trigger = CumulativeGrowingDegreeDaysTrigger(1f)
        val events = listOf(
            Pair(LocalDate.of(2024, 3, 1), LifecycleEvent("start", trigger)),
            Pair(LocalDate.of(2024, 5, 15), LifecycleEvent("end", trigger)),
            Pair(LocalDate.of(2024, 7, 1), LifecycleEvent("start", trigger)),
            Pair(LocalDate.of(2024, 10, 31), LifecycleEvent("end", trigger))
        )

        val result = Ecology.getActivePeriodsForYear(2024, events, "start", "end")

        assertEquals(2, result.size)
        assertEquals(LocalDate.of(2024, 3, 1), result[0].start)
        assertEquals(LocalDate.of(2024, 5, 15), result[0].end)
        assertEquals(LocalDate.of(2024, 7, 1), result[1].start)
        assertEquals(LocalDate.of(2024, 10, 31), result[1].end)
    }

    @Test
    fun activePeriodsFiltersOutEventsFromOtherYears() {
        val trigger = CumulativeGrowingDegreeDaysTrigger(1f)
        val events = listOf(
            Pair(LocalDate.of(2023, 6, 1), LifecycleEvent("start", trigger)),
            Pair(LocalDate.of(2024, 4, 1), LifecycleEvent("start", trigger)),
            Pair(LocalDate.of(2024, 8, 1), LifecycleEvent("end", trigger)),
            Pair(LocalDate.of(2025, 3, 1), LifecycleEvent("end", trigger))
        )

        val result = Ecology.getActivePeriodsForYear(2024, events, "start", "end")

        assertEquals(1, result.size)
        assertEquals(LocalDate.of(2024, 4, 1), result[0].start)
        assertEquals(LocalDate.of(2024, 8, 1), result[0].end)
    }

    @Test
    fun activePeriodsIgnoresUnrelatedEvents() {
        val trigger = CumulativeGrowingDegreeDaysTrigger(1f)
        val events = listOf(
            Pair(LocalDate.of(2024, 2, 1), LifecycleEvent("bloom", trigger)),
            Pair(LocalDate.of(2024, 4, 1), LifecycleEvent("start", trigger)),
            Pair(LocalDate.of(2024, 6, 1), LifecycleEvent("fruit", trigger)),
            Pair(LocalDate.of(2024, 9, 1), LifecycleEvent("end", trigger))
        )

        val result = Ecology.getActivePeriodsForYear(2024, events, "start", "end")

        assertEquals(1, result.size)
        assertEquals(LocalDate.of(2024, 4, 1), result[0].start)
        assertEquals(LocalDate.of(2024, 9, 1), result[0].end)
    }

    @Test
    fun activePeriodsHandlesEndBeforeStart() {
        val trigger = CumulativeGrowingDegreeDaysTrigger(1f)
        val events = listOf(
            Pair(LocalDate.of(2024, 2, 1), LifecycleEvent("end", trigger)),
            Pair(LocalDate.of(2024, 10, 1), LifecycleEvent("start", trigger))
        )

        val result = Ecology.getActivePeriodsForYear(2024, events, "start", "end")

        assertEquals(2, result.size)
        // First period: Jan 1 to Feb 1 (end event closes implicit start)
        assertEquals(LocalDate.of(2024, 1, 1), result[0].start)
        assertEquals(LocalDate.of(2024, 2, 1), result[0].end)
        // Second period: Oct 1 to Dec 31 (start event with no end)
        assertEquals(LocalDate.of(2024, 10, 1), result[1].start)
        assertEquals(LocalDate.of(2024, 12, 31), result[1].end)
    }

    private fun mockTemperatureProvider(date: LocalDate): Range<Temperature> {
        // Sine wave with min at Feb 1 and max at Aug 1, ranging from 0C to 20C
        val dayOfYear = date.dayOfYear
        val amplitude = 10f
        val frequency = 1f
        val phaseShift = 1.5f + 32 / 365f
        val verticalShift = 10f
        val wave = SineWave(amplitude, frequency, phaseShift, verticalShift)
        val temp = wave.calculate(2 * PI.toFloat() * dayOfYear.toFloat() / 365f)
        return Range(Temperature.celsius(temp - 5f), Temperature.celsius(temp + 5f))
    }

    private fun mockDayLengthProvider(date: LocalDate): Duration {
        // Sine wave with min at Jan 1 and max at July 1, ranging from 8 to 16
        val dayOfYear = date.dayOfYear
        val amplitude = 4f
        val frequency = 1f
        val phaseShift = 1.5f
        val verticalShift = 12f
        val wave = SineWave(amplitude, frequency, phaseShift, verticalShift)
        val minutes = wave.calculate(2 * PI.toFloat() * dayOfYear.toFloat() / 365f) * 60
        return Duration.ofMinutes(minutes.toLong())
    }
}
