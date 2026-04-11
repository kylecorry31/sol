package com.kylecorry.sol.science.ecology

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.units.Temperature
import java.time.Duration
import java.time.LocalDate

data class LifecycleEventFactors(
    val lengthOfDay: Duration,
    val temperature: Range<Temperature>,
    val date: LocalDate
)