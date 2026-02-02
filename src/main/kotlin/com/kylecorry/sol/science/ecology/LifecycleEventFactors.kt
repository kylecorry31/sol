package com.kylecorry.sol.science.ecology

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.units.Temperature

data class LifecycleEventFactors(
    val cumulativeGrowingDegreeDays: Float,
    val temperatureHistory30Days: List<Range<Temperature>>,
    val cumulativeGrowingDegreeDayHistory30Days: List<Float>
)