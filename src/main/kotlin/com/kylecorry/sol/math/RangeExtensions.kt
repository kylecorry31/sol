package com.kylecorry.sol.math

fun <T : Comparable<T>> List<Range<T>>.mergeIntersecting(): List<Range<T>> {
    val newRanges = mutableListOf<Range<T>>()
    for (range in sortedBy { it.start }) {
        if (newRanges.isEmpty()) {
            newRanges.add(range)
        } else {
            val previous = newRanges.last()
            if (previous.contains(range.start)) {
                newRanges.removeAt(newRanges.lastIndex)
                newRanges.add(Range(previous.start, maxOf(previous.end, range.end)))
            } else {
                newRanges.add(range)
            }
        }
    }
    return newRanges
}
