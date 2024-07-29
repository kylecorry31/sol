package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.science.oceanography.Tide
import com.kylecorry.sol.time.Time
import com.kylecorry.sol.units.Coordinate
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

class LunitidalWaterLevelCalculator(
    private val lunitidalInterval: Duration,
    private val location: Coordinate = Coordinate.zero,
    private val lowLunitidalInterval: Duration? = null
) : IWaterLevelCalculator {

    // TODO: LRU cache
    private val upperMoonTransitMap = mutableMapOf<LocalDate, ZonedDateTime>()
    private val lowerMoonTransitMap = mutableMapOf<LocalDate, ZonedDateTime>()

    private var cachedCalculator: IWaterLevelCalculator? = null
    private var cachedCalculatorTideStart: Tide? = null
    private var cachedCalculatorTideEnd: Tide? = null

    override fun calculate(time: ZonedDateTime): Float {
        val previous = getPreviousTide(time)
        val next = getNextTide(time)

        if (previous == null || next == null) {
            return 0f
        }

        val calculator = if (previous.isHigh == next.isHigh) {
            val durationBetween = Duration.between(previous.time, next.time)
            val low = previous.time.plus(durationBetween.dividedBy(2))
            if (low.isBefore(time)) {
                getCalculator(
                    Tide.low(low),
                    Tide.high(next.time)
                )
            } else {
                getCalculator(
                    Tide.high(previous.time),
                    Tide.low(low)
                )
            }
        } else {
            getCalculator(
                previous,
                next,
            )
        }

        return calculator.calculate(time)
    }

    private fun getCalculator(previous: Tide, next: Tide): IWaterLevelCalculator {
        synchronized(this) {
            if (cachedCalculator != null && cachedCalculatorTideStart == previous && cachedCalculatorTideEnd == next) {
                return cachedCalculator!!
            }
            val calculator = RuleOfTwelfthsWaterLevelCalculator(previous, next)
            cachedCalculator = calculator
            cachedCalculatorTideStart = previous
            cachedCalculatorTideEnd = next
            return calculator
        }
    }

    private fun getPreviousTide(time: ZonedDateTime): Tide? {
        return if (lowLunitidalInterval != null) {
            val previousLow = getLowTide(time, false)
            val previousHigh = getHighTide(time, false)
            Time.getClosestPastTime(time, listOf(previousLow, previousHigh))?.let {
                if (it == previousLow) {
                    Tide.low(it)
                } else {
                    Tide.high(it)
                }
            }
        } else {
            getHighTide(time, false)?.let { Tide.high(it) }
        }
    }

    private fun getNextTide(time: ZonedDateTime): Tide? {
        return if (lowLunitidalInterval != null) {
            val nextLow = getLowTide(time, true)
            val nextHigh = getHighTide(time, true)
            Time.getClosestFutureTime(time, listOf(nextLow, nextHigh))?.let {
                if (it == nextLow) {
                    Tide.low(it)
                } else {
                    Tide.high(it)
                }
            }
        } else {
            getHighTide(time, true)?.let { Tide.high(it) }
        }
    }

    private fun getTide(time: ZonedDateTime, isHigh: Boolean, isNext: Boolean): ZonedDateTime? {
        val interval = if (isHigh) lunitidalInterval else (lowLunitidalInterval ?: lunitidalInterval)
        return shortCircuitTransitTime(time, interval, isNext)
    }

    private fun shortCircuitTransitTime(time: ZonedDateTime, interval: Duration, isNext: Boolean): ZonedDateTime? {
        val shortCircuitDuration = Duration.ofHours(12)
        if (isNext) {
            val lookups = listOf(
                0L to true,
                0L to false,
                1L to true,
                1L to false,
                -1L to true,
                -1L to false
            )
            val times = mutableListOf<ZonedDateTime>()
            for (lookup in lookups) {
                val transit = if (lookup.second) {
                    getUpperMoonTransit(time.plusDays(lookup.first))?.plus(interval)
                } else {
                    getLowerMoonTransit(time.plusDays(lookup.first))?.plus(interval)
                }
                if (transit != null && transit.isAfter(time) && transit.isBefore(time.plus(shortCircuitDuration))) {
                    return transit
                } else if (transit != null) {
                    times.add(transit)
                }
            }
            return Time.getClosestFutureTime(time, times)
        } else {
            val lookups = listOf(
                0L to true,
                0L to false,
                -1L to true,
                -1L to false,
                1L to true,
                1L to false
            )
            val times = mutableListOf<ZonedDateTime>()
            for (lookup in lookups) {
                val transit = if (lookup.second) {
                    getUpperMoonTransit(time.plusDays(lookup.first))?.plus(interval)
                } else {
                    getLowerMoonTransit(time.plusDays(lookup.first))?.plus(interval)
                }
                if (transit != null && transit.isBefore(time) && transit.isAfter(time.minus(shortCircuitDuration))) {
                    return transit
                } else if (transit != null) {
                    times.add(transit)
                }
            }
            return Time.getClosestPastTime(time, times)
        }
    }

    private fun getHighTide(time: ZonedDateTime, isNext: Boolean): ZonedDateTime? {
        return getTide(time, true, isNext)
    }

    private fun getLowTide(time: ZonedDateTime, isNext: Boolean): ZonedDateTime? {
        return getTide(time, false, isNext)
    }

    private fun getKey(time: ZonedDateTime): LocalDate {
        return time.toLocalDate()
    }

    private fun getUpperMoonTransit(time: ZonedDateTime): ZonedDateTime? {
        if (upperMoonTransitMap.containsKey(getKey(time))) {
            return upperMoonTransitMap[getKey(time)]
        }
        val transit = Astronomy.getMoonEvents(time, location).transit ?: return null
        upperMoonTransitMap[getKey(time)] = transit
        return transit
    }

    private fun getLowerMoonTransit(time: ZonedDateTime): ZonedDateTime? {
        if (lowerMoonTransitMap.containsKey(getKey(time))) {
            return lowerMoonTransitMap[getKey(time)]
        }
        val transit = Astronomy.getMoonEvents(time, Coordinate(location.latitude, location.longitude + 180)).transit
            ?: return null
        lowerMoonTransitMap[getKey(time)] = transit
        return transit
    }
}