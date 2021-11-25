package com.kylecorry.sol.math.filters

import kotlin.math.abs

class ProximityChangeFilter<T>(
    private val changeThreshold: Float,
    private val distanceFn: (start: T, end: T) -> Float
) {

    fun filter(points: List<T>): List<T> {

        if (points.isEmpty()) {
            return emptyList()
        }

        val filtered = mutableListOf<T>()
        var lastValid = points.first()
        filtered.add(lastValid)

        for (i in 1 until points.size) {
            val current = points[i]
            val change = abs(distanceFn(current, lastValid))

            if (change >= changeThreshold) {
                lastValid = current
                filtered.add(current)
            }
        }

        return filtered
    }

}