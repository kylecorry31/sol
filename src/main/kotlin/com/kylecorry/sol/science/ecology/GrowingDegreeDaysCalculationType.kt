package com.kylecorry.sol.science.ecology

enum class GrowingDegreeDaysCalculationType {
    /**
     * Always uses the minimum and maximum temperature
     */
    MinMax,

    /**
     * Uses the minimum and maximum temperature, but if min < base it will use base instead of min
     */
    BaseMax
}