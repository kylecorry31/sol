package com.kylecorry.sol.math.filters

class ProximityChangeFilter1D(
    private val changeThreshold: Float
) : IFilter1D {

    override fun filter(data: List<Float>): List<Float> {
        val filter = ProximityChangeFilterGeneric<Float>(
            changeThreshold,
            { previous, _ -> previous }) { start, end ->
            end - start
        }
        return filter.filter(data)
    }

}