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

}
