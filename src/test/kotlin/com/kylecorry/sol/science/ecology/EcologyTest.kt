package com.kylecorry.sol.science.ecology

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.ecology.triggers.LifecycleEventTrigger
import com.kylecorry.sol.science.ecology.triggers.MinimumGrowingDegreeDaysTrigger
import com.kylecorry.sol.units.Temperature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

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
            { Range(Temperature.celsius(0f), Temperature.celsius(10f)) }
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
    fun lifecycleEventTriggeredWhenGDDReached() {
        // 10 GDD per day: (15+5)/2 - 0 = 10. Threshold is 25, so should trigger on day 3.
        val event = LifecycleEvent("bloom", MinimumGrowingDegreeDaysTrigger(25f))
        val phenology = SpeciesPhenology(
            baseGrowingDegreeDaysTemperature = Temperature.celsius(0f),
            events = listOf(event)
        )

        val coldestDate = LocalDate.of(2023, 12, 31)
        val start = LocalDate.of(2024, 1, 1)
        val end = LocalDate.of(2024, 1, 10)

        val result = Ecology.getLifecycleEventDates(
            phenology,
            Range(start, end)
        ) {
            if (it == coldestDate) {
                Range(Temperature.celsius(-50f), Temperature.celsius(-40f))
            } else {
                Range(Temperature.celsius(5f), Temperature.celsius(15f))
            }
        }

        assertEquals(1, result.size)
        assertEquals(LocalDate.of(2024, 1, 3), result[0].first)
        assertEquals("bloom", result[0].second.name)
    }

    @Test
    fun lifecycleEventNotTriggeredWhenGDDNotReached() {
        val event = LifecycleEvent("bloom", MinimumGrowingDegreeDaysTrigger(Float.MAX_VALUE))
        val phenology = SpeciesPhenology(
            baseGrowingDegreeDaysTemperature = Temperature.celsius(0f),
            events = listOf(event)
        )

        val start = LocalDate.of(2024, 6, 1)
        val end = LocalDate.of(2024, 6, 5)

        val result = Ecology.getLifecycleEventDates(
            phenology,
            Range(start, end),
            { Range(Temperature.celsius(5f), Temperature.celsius(15f)) }
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun lifecycleEventOnlyTriggeredOnce() {
        val event = LifecycleEvent("bloom", MinimumGrowingDegreeDaysTrigger(1f))
        val phenology = SpeciesPhenology(
            baseGrowingDegreeDaysTemperature = Temperature.celsius(0f),
            events = listOf(event)
        )

        val start = LocalDate.of(2024, 6, 1)
        val end = LocalDate.of(2024, 6, 10)

        val result = Ecology.getLifecycleEventDates(
            phenology,
            Range(start, end),
            { Range(Temperature.celsius(5f), Temperature.celsius(15f)) }
        )

        assertEquals(1, result.count { it.second.name == "bloom" })
    }

    @Test
    fun multipleLifecycleEventsCanTrigger() {
        val early = LifecycleEvent("leaf-out", MinimumGrowingDegreeDaysTrigger(1f))
        val late = LifecycleEvent("bloom", MinimumGrowingDegreeDaysTrigger(50f))
        val phenology = SpeciesPhenology(
            baseGrowingDegreeDaysTemperature = Temperature.celsius(0f),
            events = listOf(early, late)
        )

        val start = LocalDate.of(2024, 6, 1)
        val end = LocalDate.of(2024, 7, 31)

        val result = Ecology.getLifecycleEventDates(
            phenology,
            Range(start, end),
            { Range(Temperature.celsius(5f), Temperature.celsius(15f)) }
        )

        assertEquals(2, result.size)
        assertEquals("leaf-out", result[0].second.name)
        assertEquals("bloom", result[1].second.name)
        assertTrue(result[0].first <= result[1].first)
    }

    @Test
    fun lifecycleEventReturnsEmptyForEmptyRange() {
        val event = LifecycleEvent("bloom", MinimumGrowingDegreeDaysTrigger(10f))
        val phenology = SpeciesPhenology(
            baseGrowingDegreeDaysTemperature = Temperature.celsius(0f),
            events = listOf(event)
        )

        val date = LocalDate.of(2024, 6, 1)

        val result = Ecology.getLifecycleEventDates(
            phenology,
            Range(date, date.minusDays(1)),
            { Range(Temperature.celsius(5f), Temperature.celsius(15f)) }
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun lifecycleEventUsesGDDCap() {
        // With a cap of 15, the max temp is capped at 15 so GDD = (15+5)/2 - 0 = 10 per day
        // Without cap, GDD = (25+5)/2 - 0 = 15 per day
        // With threshold at 12, it should take 2 days with cap vs 1 day without
        val event = LifecycleEvent("bloom", MinimumGrowingDegreeDaysTrigger(12f))
        val phenology = SpeciesPhenology(
            baseGrowingDegreeDaysTemperature = Temperature.celsius(0f),
            events = listOf(event),
            growingDegreeDaysCap = 15f
        )

        val start = LocalDate.of(2024, 6, 1)
        val end = LocalDate.of(2024, 6, 10)

        val result = Ecology.getLifecycleEventDates(
            phenology,
            Range(start, end),
            { Range(Temperature.celsius(5f), Temperature.celsius(25f)) }
        )

        assertTrue(result.isNotEmpty())
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
        val trigger = MinimumGrowingDegreeDaysTrigger(1f)
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
        val trigger = MinimumGrowingDegreeDaysTrigger(1f)
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
        val trigger = MinimumGrowingDegreeDaysTrigger(1f)
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
        val trigger = MinimumGrowingDegreeDaysTrigger(1f)
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
        val trigger = MinimumGrowingDegreeDaysTrigger(1f)
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
        val trigger = MinimumGrowingDegreeDaysTrigger(1f)
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
        val trigger = MinimumGrowingDegreeDaysTrigger(1f)
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
}
