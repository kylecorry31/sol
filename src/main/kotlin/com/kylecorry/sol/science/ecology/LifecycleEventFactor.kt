package com.kylecorry.sol.science.ecology

data class LifecycleEventFactor<T>(
    val current: T,
    val history: List<T>,
)