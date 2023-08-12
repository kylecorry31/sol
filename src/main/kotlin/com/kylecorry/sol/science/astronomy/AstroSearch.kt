package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.time.Time.middle
import java.time.Duration
import java.time.Instant

internal object AstroSearch {

    /**
     * The maximum number of iterations to perform when searching for an event
     */
    private val maxIterations = 100

    /**
     * Finds the start time of an event. The range should start before the event and end during the event.
     *
     * @param range the range to search
     * @param precision the precision of the search
     * @param predicate the predicate to test
     * @return the start time or null if not found
     */
    fun findStart(range: Range<Instant>, precision: Duration, predicate: (Instant) -> Boolean): Instant? {
        var left = range.start
        var right = range.end
        var iterations = 0
        var wasFound = false

        while (Duration.between(left, right) > precision && iterations < maxIterations) {
            val mid = left.plusMillis((right.toEpochMilli() - left.toEpochMilli()) / 2)
            if (predicate(mid)) {
                wasFound = true
                right = mid
            } else {
                left = mid
            }
            iterations++
        }

        return if (wasFound) {
            right
        } else {
            null
        }
    }

    /**
     * Finds the end time of an event. The range should start during the event and end after the event.
     *
     * @param range the range to search
     * @param precision the precision of the search
     * @param predicate the predicate to test
     * @return the end time or null if not found
     */
    fun findEnd(range: Range<Instant>, precision: Duration, predicate: (Instant) -> Boolean): Instant? {
        var left = range.start
        var right = range.end
        var iterations = 0
        var wasFound = false

        while (Duration.between(left, right) > precision && iterations < maxIterations) {
            val mid = left.plusMillis((right.toEpochMilli() - left.toEpochMilli()) / 2)
            if (predicate(mid)) {
                wasFound = true
                left = mid
            } else {
                right = mid
            }
            iterations++
        }

        return if (wasFound) {
            left
        } else {
            null
        }
    }

    /**
     * Finds the peak of an event. The range should be during the event.
     *
     * @param range the range to search
     * @param precision the precision of the search
     * @param producer the function to produce a value for a given time
     * @return the peak time
     */
    fun findPeak(range: Range<Instant>, precision: Duration, producer: (Instant) -> Float): Instant {
        var left = range.start
        var right = range.end
        var iterations = 0
        while (Duration.between(left, right) > precision && iterations < maxIterations) {
            val remaining = Duration.between(left, right).toMillis()
            val midLeft = left.plusMillis(remaining / 3)
            val midRight = left.plusMillis(remaining * 2 / 3)
            val valueLeft = producer(midLeft)
            val valueRight = producer(midRight)
            if (valueLeft < valueRight) {
                left = midLeft
            } else {
                right = midRight
            }
            iterations++
        }

        return if (producer(left) > producer(right)) {
            left
        } else {
            right
        }
    }

    /**
     * Searches for an event within a range. Uses bidirectional search within the range. The event time returned
     * only indicates a time that the event is occurring, not necessarily a start, peak, or end time.
     *
     * @param range the range to search
     * @param precision the precision of the search
     * @param start the start time to search from, defaults to the middle of the range
     * @param predicate the predicate to test
     * @return the time of the event or null if not found
     */
    fun findEvent(
        range: Range<Instant>,
        precision: Duration,
        start: Instant = range.middle(),
        predicate: (Instant) -> Boolean
    ): Instant? {
        var left = start
        var right = start

        while (left >= range.start || right <= range.end) {
            if (left >= range.start && predicate(left)) {
                return left
            }
            if (right <= range.end && right != left && predicate(right)) {
                return right
            }
            left -= precision
            right += precision
        }

        return null
    }

}