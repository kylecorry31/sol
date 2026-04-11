package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.Vector2

class ProximityChangeFilter2D(
    private val changeThreshold: Float
) : IFilter2D {

    override fun filter(data: List<Vector2>): List<Vector2> {
        val filter = ProximityChangeFilterGeneric<Vector2>(
            changeThreshold,
            { previous, _ -> previous }) { start, end ->
            start.distanceTo(end)
        }
        return filter.filter(data)
    }

}