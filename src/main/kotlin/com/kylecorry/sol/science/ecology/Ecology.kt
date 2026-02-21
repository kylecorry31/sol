package com.kylecorry.sol.science.ecology

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.units.Temperature
import java.time.Duration
import java.time.LocalDate

object Ecology {

    fun getGrowingDegreeDays(
        temperature: Range<Temperature>,
        baseTemperature: Temperature,
        limit: Float = Float.MAX_VALUE,
        calculationType: GrowingDegreeDaysCalculationType = GrowingDegreeDaysCalculationType.MinMax
    ): Float {

        val max = temperature.end.celsius().value.coerceAtMost(limit)
        var min = temperature.start.celsius().value

        if (calculationType == GrowingDegreeDaysCalculationType.BaseMax && min < baseTemperature.celsius().value) {
            min = baseTemperature.celsius().value
        }

        val average = (max + min) / 2

        return (average - baseTemperature.celsius().value).coerceAtLeast(0f)
    }

    fun getColdDegreeDays(
        temperature: Range<Temperature>,
        baseTemperature: Temperature,
        limit: Float = Float.MAX_VALUE
    ): Float {
        val max = temperature.end.celsius().value.coerceAtMost(limit)
        val min = temperature.start.celsius().value
        val average = (max + min) / 2
        return (baseTemperature.celsius().value - average).coerceAtLeast(0f)
    }

    fun getCumulativeGrowingDegreeDays(
        dates: List<LocalDate>,
        baseTemperature: Temperature,
        temperatureProvider: (LocalDate) -> Range<Temperature>,
        limit: Float = Float.MAX_VALUE,
        calculationType: GrowingDegreeDaysCalculationType = GrowingDegreeDaysCalculationType.MinMax
    ): List<Pair<LocalDate, Float>> {
        val earliestDate = dates.minOrNull() ?: return emptyList()

        // Search the full year prior to the first date for the lowest temperature
        var startDate = earliestDate.minusYears(1)
        var lowestTemperature = temperatureProvider(startDate)
        var currentDate = startDate.plusDays(1)
        while (currentDate < earliestDate) {
            val temperature = temperatureProvider(currentDate)
            if (temperature.start.celsius().value < lowestTemperature.start.celsius().value) {
                lowestTemperature = temperature
                startDate = currentDate
            }
            currentDate = currentDate.plusDays(1)
        }

        // Calculate from that day forward (reset every year)
        currentDate = earliestDate
        val gdd = mutableListOf<Pair<LocalDate, Float>>()
        val queue = dates.toMutableSet()
        var cumulative = 0f
        while (queue.isNotEmpty()) {
            if (currentDate >= startDate.plusYears(1)) {
                startDate = startDate.plusYears(1)
                cumulative = 0f
            }
            cumulative += getGrowingDegreeDays(
                temperatureProvider(currentDate),
                baseTemperature,
                limit,
                calculationType
            )
            if (currentDate in queue) {
                queue.remove(currentDate)
                gdd.add(Pair(currentDate, cumulative))
            }
            currentDate = currentDate.plusDays(1)
        }
        return gdd
    }

    fun getLifecycleEventDates(
        events: List<LifecycleEvent>,
        dateRange: Range<LocalDate>,
        dayLengthProvider: (LocalDate) -> Duration,
        temperatureProvider: (LocalDate) -> Range<Temperature>,
    ): List<Pair<LocalDate, LifecycleEvent>> {
        val lifecycleEvents = mutableListOf<Pair<LocalDate, LifecycleEvent>>()
        val hits = mutableSetOf<LifecycleEvent>()
        var date = dateRange.start.minusYears(1)
        events.forEach { it.trigger.reset() }
        while (date <= dateRange.end) {
            val temperature = temperatureProvider(date)
            val dayLength = dayLengthProvider(date)

            val factors = LifecycleEventFactors(
                dayLength,
                temperature,
                date
            )

            for (event in events) {
                if (event.trigger.isTriggered(factors)) {
                    val isNew = hits.add(event)

                    if (isNew && date >= dateRange.start) {
                        lifecycleEvents.add(date to event)
                    }
                } else {
                    hits.remove(event)
                }
            }

            // Add all active events that occurred and are currently active as of the start date
            if (date == dateRange.start) {
                hits.forEach { event ->
                    lifecycleEvents.add(date to event)
                }
            }

            date = date.plusDays(1)
        }
        return lifecycleEvents.sortedBy { it.first }
    }

    fun getActivePeriodsForYear(
        year: Int,
        events: List<Pair<LocalDate, LifecycleEvent>>,
        activeStart: String,
        activeEnd: String
    ): List<Range<LocalDate>> {
        val activePeriods = mutableListOf<Range<LocalDate>>()
        var startDate: LocalDate = LocalDate.of(year, 1, 1)
        var hasStartDate = false
        for (event in events.filter { it.first.year == year }) {
            if (event.second.name == activeEnd) {
                activePeriods.add(Range(startDate, event.first))
                hasStartDate = false
            } else if (event.second.name == activeStart) {
                startDate = event.first
                hasStartDate = true
            }
        }

        if (hasStartDate) {
            activePeriods.add(Range(startDate, LocalDate.of(year, 12, 31)))
        }

        return activePeriods
    }

}
