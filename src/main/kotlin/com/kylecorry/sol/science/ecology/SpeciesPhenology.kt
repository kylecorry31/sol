package com.kylecorry.sol.science.ecology

import com.kylecorry.sol.units.Temperature

data class SpeciesPhenology(
    val baseGrowingDegreeDaysTemperature: Temperature,
    val events: List<LifecycleEvent>,
    val growingDegreeDaysCap: Float = Float.MAX_VALUE,
    val growingDegreeDaysCalculationType: GrowingDegreeDaysCalculationType = GrowingDegreeDaysCalculationType.MinMax
)