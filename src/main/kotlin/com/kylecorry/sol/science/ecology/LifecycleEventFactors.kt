package com.kylecorry.sol.science.ecology

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.units.Temperature
import java.time.Duration

data class LifecycleEventFactors(
    val cumulativeGrowingDegreeDays: Float,
    val lengthOfDay: Duration,
    val temperatureHistory30Days: List<Range<Temperature>>,
    val cumulativeGrowingDegreeDayHistory30Days: List<Float>,
    val lengthOfDayHistory30Days: List<Duration>,
)