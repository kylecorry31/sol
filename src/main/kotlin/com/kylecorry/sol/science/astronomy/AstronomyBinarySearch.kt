package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.math.Range
import java.time.Duration
import java.time.Instant

internal class AstronomyBinarySearch {

    private val maxIterations = 20

    fun findStartTime(range: Range<Instant>, precision: Duration, predicate: (Instant) -> Boolean): Instant? {
        var left = range.start
        var right = range.end
        var iterations = 0

        // Use binary search to find the start time - the predicate will be false at the start and true at the end
        while (Duration.between(left, right) > precision && iterations < maxIterations) {
            val mid = left.plusMillis((right.toEpochMilli() - left.toEpochMilli()) / 2)
            if (predicate(mid)) {
                right = mid
            } else {
                left = mid.plusMillis(1)
            }
            iterations++
        }

        return if (left != range.start){
            left
        } else {
            null
        }
    }

    fun findEndTime(range: Range<Instant>, precision: Duration, predicate: (Instant) -> Boolean): Instant? {
        var left = range.start
        var right = range.end
        var iterations = 0

        // Use binary search to find the end time - the predicate will be true at the start and false at the end
        while (Duration.between(left, right) > precision && iterations < maxIterations) {
            val mid = left.plusMillis((right.toEpochMilli() - left.toEpochMilli()) / 2)
            if (predicate(mid)) {
                left = mid
            } else {
                right = mid.minusMillis(1)
            }
            iterations++
        }

        return if (right != range.end){
            right
        } else {
            null
        }
    }

    fun findPeak(range: Range<Instant>, precision: Duration, producer: (Instant) -> Float): Instant {
        var start = range.start
        var end = range.end
        var iterations = 0
        while (Duration.between(start, end) > precision && iterations < maxIterations) {
            val remaining = Duration.between(start, end).toMillis()
            val midLeft = start.plusMillis(remaining / 3)
            val midRight = start.plusMillis(remaining * 2 / 3)
            val valueLeft = producer(midLeft)
            val valueRight = producer(midRight)
            if (valueLeft < valueRight) {
                start = midLeft
            } else {
                end = midRight
            }
            iterations++
        }

        return if (producer(start) > producer(end)) {
            start
        } else {
            end
        }
    }

}