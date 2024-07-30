package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.math.RingBuffer
import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.science.oceanography.Tide
import com.kylecorry.sol.time.Time
import com.kylecorry.sol.units.Coordinate
import java.time.Duration
import java.time.ZonedDateTime

class LunitidalWaterLevelCalculator(
    private val lunitidalInterval: Duration,
    private val location: Coordinate = Coordinate.zero,
    private val lowLunitidalInterval: Duration? = null
) : IWaterLevelCalculator {

    private val moonTransits = RingBuffer<ZonedDateTime>(24)

    private val antipodeLocation = location.antipode

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
        val tides = getTideTimes(time, interval)
        return if (isNext) {
            Time.getClosestFutureTime(time, tides)
        } else {
            Time.getClosestPastTime(time, tides)
        }
    }

    private fun getHighTide(time: ZonedDateTime, isNext: Boolean): ZonedDateTime? {
        return getTide(time, true, isNext)
    }

    private fun getLowTide(time: ZonedDateTime, isNext: Boolean): ZonedDateTime? {
        return getTide(time, false, isNext)
    }

    private fun getUpperMoonTransit(time: ZonedDateTime): ZonedDateTime? {
        return Astronomy.getMoonEvents(time, location).transit
    }

    private fun getLowerMoonTransit(time: ZonedDateTime): ZonedDateTime? {
        return Astronomy.getMoonEvents(time, antipodeLocation).transit
    }

    private fun getTideTimes(time: ZonedDateTime, interval: Duration): List<ZonedDateTime> {
        val shortCircuitDuration = Duration.ofHours(14)
        val tides = moonTransits.toList().map { it.plus(interval) }.toMutableList()

        var before = tides.firstOrNull { it.isBefore(time) && it.isAfter(time.minus(shortCircuitDuration)) }
        var after = tides.firstOrNull { it.isAfter(time) && it.isBefore(time.plus(shortCircuitDuration)) }

        if (before == null) {
            val maxDays = 2
            var index = 0
            while (before == null && index < maxDays) {
                val beforeUpper = getUpperMoonTransit(time.minusDays(index.toLong()))
                val beforeLower = getLowerMoonTransit(time.minusDays(index.toLong()))
                if (beforeUpper != null && !tides.contains(beforeUpper)) {
                    tides.add(beforeUpper.plus(interval))
                    moonTransits.add(beforeUpper)
                }

                if (beforeLower != null && !tides.contains(beforeLower)) {
                    tides.add(beforeLower.plus(interval))
                    moonTransits.add(beforeLower)
                }

                val closest =
                    Time.getClosestPastTime(time, listOf(beforeUpper?.plus(interval), beforeLower?.plus(interval)))

                if (closest != null && closest.isBefore(time) && closest.isAfter(time.minus(shortCircuitDuration))) {
                    before = closest
                }

                index++
            }
        }

        if (after == null) {
            val maxDays = 2
            var index = 0
            while (after == null && index < maxDays) {
                val afterUpper = getUpperMoonTransit(time.plusDays(index.toLong()))
                val afterLower = getLowerMoonTransit(time.plusDays(index.toLong()))
                if (afterUpper != null && !tides.contains(afterUpper)) {
                    tides.add(afterUpper.plus(interval))
                    moonTransits.add(afterUpper)
                }

                if (afterLower != null && !tides.contains(afterLower)) {
                    tides.add(afterLower.plus(interval))
                    moonTransits.add(afterLower)
                }

                val closest =
                    Time.getClosestFutureTime(time, listOf(afterUpper?.plus(interval), afterLower?.plus(interval)))

                if (closest != null && closest.isAfter(time) && closest.isBefore(time.plus(shortCircuitDuration))) {
                    after = closest
                }

                index++
            }
        }

        return tides
    }
}