package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.time.Time.atEndOfDay
import com.kylecorry.sol.time.Time.atStartOfDay
import com.kylecorry.sol.time.Time.getClosestFutureTime
import com.kylecorry.sol.time.Time.getClosestPastTime
import com.kylecorry.sol.units.Coordinate
import java.time.Duration
import java.time.ZonedDateTime

internal object AboveHorizonFacade {
    fun getAboveHorizonTimes(
        location: Coordinate,
        time: ZonedDateTime,
        nextRiseOffset: Duration,
        isUpPredicate: (Coordinate, ZonedDateTime) -> Boolean,
        riseSetTransitTimesProducer: (Coordinate, ZonedDateTime) -> RiseSetTransitTimes
    ): Range<ZonedDateTime>? {
        // If it is up, use the last rise to the next set
        // If it is down and is less than nextRiseOffset from the next rise, use the next rise to the next set
        // If it is down and is greater than nextRiseOffset from the next rise, use the last rise to the last set
        val isUp = isUpPredicate(location, time)

        val yesterday = riseSetTransitTimesProducer(location, time.minusDays(1))
        val today = riseSetTransitTimesProducer(location, time)
        val tomorrow = riseSetTransitTimesProducer(location, time.plusDays(1))

        val lastRise =
            getClosestPastTime(time, listOfNotNull(yesterday.rise, today.rise, tomorrow.rise))
        val nextRise = getClosestFutureTime(
            time,
            listOfNotNull(yesterday.rise, today.rise, tomorrow.rise)
        )
        val lastSet =
            getClosestPastTime(time, listOfNotNull(yesterday.set, today.set, tomorrow.set))
        val nextSet =
            getClosestFutureTime(time, listOfNotNull(yesterday.set, today.set, tomorrow.set))

        if (isUp) {
            return Range(lastRise ?: time.atStartOfDay(), nextSet ?: time.atEndOfDay())
        }

        if (nextRise == null || Duration.between(time, nextRise) > nextRiseOffset) {
            if (lastRise == null && lastSet == null) {
                return null
            }

            return Range(lastRise ?: time.atStartOfDay(), lastSet ?: time.atEndOfDay())
        }

        return Range(nextRise, nextSet ?: time.atEndOfDay())
    }
}
