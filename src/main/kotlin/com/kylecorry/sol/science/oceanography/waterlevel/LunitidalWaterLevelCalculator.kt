package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.math.Range
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
    private val lowLunitidalInterval: Duration? = null,
    private val waterLevelRange: Range<Float>? = null
) : IWaterLevelCalculator {

    init {
        require(waterLevelRange == null || waterLevelRange.start <= waterLevelRange.end) {
            "waterLevelRange start must be less than or equal to end."
        }
    }

    private val moonTransitCache = RingBuffer<ZonedDateTime>(CACHE_SIZE)
    private val antipodeLocation = location.antipode
    private var cachedCalculator: IWaterLevelCalculator? = null
    private var cachedCalculatorTideStart: Tide? = null
    private var cachedCalculatorTideEnd: Tide? = null

    override fun calculate(time: ZonedDateTime): Float {
        val previous = getClosestTide(time, false)
        val next = getClosestTide(time, true)

        if (previous == null || next == null) {
            return 0f
        }

        check(previous != next)

        val calculator = if (previous.isHigh == next.isHigh) {
            val low = getLowTideEstimate(previous.time, next.time)
            if (low.isBefore(time)) {
                getCalculator(
                    Tide.low(low, waterLevelRange?.start),
                    Tide.high(next.time, waterLevelRange?.end)
                )
            } else {
                getCalculator(
                    Tide.high(previous.time, waterLevelRange?.end),
                    Tide.low(low, waterLevelRange?.start)
                )
            }
        } else {
            getCalculator(previous, next)
        }

        val lowestLevel = waterLevelRange?.start ?: -1f
        val highestLevel = waterLevelRange?.end ?: 1f

        val level = calculator.calculate(time).coerceIn(lowestLevel, highestLevel)
        check(level.isFinite())
        check(level in lowestLevel..highestLevel)
        return level
    }

    private fun getLowTideEstimate(timeHighPrevious: ZonedDateTime, timeHighNext: ZonedDateTime): ZonedDateTime {
        require(timeHighPrevious.isBefore(timeHighNext))
        val durationBetween = Duration.between(timeHighPrevious, timeHighNext)
        return timeHighPrevious.plus(durationBetween.dividedBy(2))
    }

    private fun getCalculator(previous: Tide, next: Tide): IWaterLevelCalculator {
        require(previous.time.isBefore(next.time))
        require(previous.isHigh != next.isHigh)
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

    private fun getClosestTide(time: ZonedDateTime, isNext: Boolean): Tide? {
        return if (lowLunitidalInterval != null) {
            val previousLow = getClosestLowTide(time, isNext)
            val previousHigh = getClosestHighTide(time, isNext)
            getClosestTime(time, listOf(previousLow, previousHigh), isNext)?.let {
                if (it == previousLow) {
                    Tide.low(it, waterLevelRange?.start)
                } else {
                    Tide.high(it, waterLevelRange?.end)
                }
            }
        } else {
            val previousHigh = getClosestHighTide(time, false)
            val nextHigh = getClosestHighTide(time, true)
            val low = if (previousHigh != null && nextHigh != null) {
                getLowTideEstimate(previousHigh, nextHigh)
            } else {
                null
            }
            getClosestTime(time, listOf(previousHigh, low), isNext)?.let {
                if (it == previousHigh) {
                    Tide.high(it, waterLevelRange?.end)
                } else {
                    Tide.low(it, waterLevelRange?.start)
                }
            }
        }
    }

    private fun getClosestHighTide(time: ZonedDateTime, isNext: Boolean): ZonedDateTime? {
        return getClosestTime(time, getTideTimes(time, lunitidalInterval), isNext)
    }

    private fun getClosestLowTide(time: ZonedDateTime, isNext: Boolean): ZonedDateTime? {
        return getClosestTime(time, getTideTimes(time, lowLunitidalInterval ?: lunitidalInterval), isNext)
    }

    private fun getUpperMoonTransit(time: ZonedDateTime): ZonedDateTime? {
        return Astronomy.getMoonEvents(time, location).transit
    }

    private fun getLowerMoonTransit(time: ZonedDateTime): ZonedDateTime? {
        return Astronomy.getMoonEvents(
            time.withZoneSameInstant(
                Time.getApproximateTimeZone(
                    antipodeLocation
                )
            ), antipodeLocation
        ).transit
    }

    private fun isBetween(time: ZonedDateTime, start: ZonedDateTime, end: ZonedDateTime): Boolean {
        return time.isAfter(start) && time.isBefore(end)
    }

    private fun getTideTimes(time: ZonedDateTime, interval: Duration): List<ZonedDateTime> {
        val tides = moonTransitCache.toList().map { it.plus(interval) }.toMutableList()

        val hasTideBefore =
            tides.any { isBetween(it, time.minus(TIME_BETWEEN_TIDES_MAX), time) }
        val hasTideAfter =
            tides.any { isBetween(it, time, time.plus(TIME_BETWEEN_TIDES_MAX)) }

        if (!hasTideBefore) {
            populateTidesForTime(time, tides, interval, true)
        }

        if (!hasTideAfter) {
            populateTidesForTime(time, tides, interval, false)
        }

        return tides
    }

    private fun populateTidesForTime(
        startTime: ZonedDateTime,
        tides: MutableList<ZonedDateTime>,
        interval: Duration,
        isBefore: Boolean
    ) {
        val offsetMultiplier = if (isBefore) -1 else 1
        repeat(SEARCH_MAX_DAYS) { index ->
            check(index < SEARCH_MAX_DAYS)
            val time = startTime.plusDays(offsetMultiplier * index.toLong())

            if (isBefore) {
                check(time == startTime || time.isBefore(startTime))
            } else {
                check(time == startTime || time.isAfter(startTime))
            }

            val upper = getUpperMoonTransit(time)
            val lower = getLowerMoonTransit(time)
            // TODO: Check if it approximately contains, in case the time is slightly off
            if (upper != null && !tides.contains(upper.plus(interval))) {
                val wasAddedToTides = tides.add(upper.plus(interval))
                check(wasAddedToTides)
                val wasAddedToCache = moonTransitCache.add(upper)
                check(wasAddedToCache)
            }

            // TODO: Check if it approximately contains, in case the time is slightly off
            if (lower != null && !tides.contains(lower.plus(interval))) {
                val wasAddedToTides = tides.add(lower.plus(interval))
                check(wasAddedToTides)
                val wasAddedToCache = moonTransitCache.add(lower)
                check(wasAddedToCache)
            }

            val times = listOf(upper?.plus(interval), lower?.plus(interval))
            val closest = getClosestTime(startTime, times, !isBefore)
            if (closest != null) {
                val furthestTime = startTime.plus(TIME_BETWEEN_TIDES_MAX.multipliedBy(offsetMultiplier.toLong()))

                if (isBefore){
                    check(furthestTime.isBefore(startTime))
                } else {
                    check(furthestTime.isAfter(startTime))
                }

                if (isBefore && isBetween(closest, furthestTime, startTime)) {
                    return
                }

                if (!isBefore && isBetween(closest, startTime, furthestTime)) {
                    return
                }

            }
        }
    }

    private fun getClosestTime(
        currentTime: ZonedDateTime,
        times: List<ZonedDateTime?>,
        isNext: Boolean
    ): ZonedDateTime? {
        return if (isNext) {
            Time.getClosestFutureTime(currentTime, times)
        } else {
            Time.getClosestPastTime(currentTime, times)
        }
    }

    companion object {
        private val TIME_BETWEEN_TIDES_MAX = Duration.ofHours(14)
        private const val SEARCH_MAX_DAYS = 2
        private const val CACHE_SIZE = 24
    }
}