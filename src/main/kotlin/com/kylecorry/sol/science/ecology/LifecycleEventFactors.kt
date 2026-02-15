package com.kylecorry.sol.science.ecology

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.units.Temperature
import java.time.Duration

data class LifecycleEventFactors(
    val cumulativeGrowingDegreeDays: LifecycleEventFactor<Float>,
    val lengthOfDay: LifecycleEventFactor<Duration>,
    val temperature: LifecycleEventFactor<Range<Temperature>>
)