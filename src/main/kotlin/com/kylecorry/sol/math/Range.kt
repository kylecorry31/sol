package com.kylecorry.sol.math

data class Range<T : Comparable<T>>(val start: T, val end: T) {
    operator fun contains(value: T): Boolean {
        return value in start..end
    }

    fun clamp(value: T): T {
        if (value > end) return end
        if (value < start) return start
        return value
    }

    fun intersection(other: Range<T>): Range<T>? {
        if (other.start > end || other.end < start) {
            return null
        }

        return Range(maxOf(start, other.start), minOf(end, other.end))
    }

    companion object {
        fun <T : Comparable<T>> intersection(
            ranges: List<Range<T>>,
            ignoreNoIntersection: Boolean = false,
            stopWhenNoIntersection: Boolean = false
        ): Range<T>? {
            if (ranges.isEmpty()) {
                return null
            }

            var intersection: Range<T>? = ranges[0]
            for (i in 1 until ranges.size) {
                val range = ranges[i]
                val newIntersection = intersection?.intersection(range)
                intersection = if (newIntersection == null) {
                    if (stopWhenNoIntersection) {
                        return intersection
                    }
                    if (ignoreNoIntersection) {
                        intersection
                    } else {
                        return null
                    }
                } else {
                    newIntersection
                }
            }

            return intersection

        }
    }

}
