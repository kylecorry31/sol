package com.kylecorry.sol.science.astronomy.eclipse

import kotlin.time.Duration
import kotlinx.datetime.Instant

data class Eclipse(
    val start: Instant,
    val end: Instant,
    val magnitude: Float,
    val obscuration: Float,
    val maximum: Instant = start.plus(Duration.between(start, end).dividedBy(2))
) {
    val duration: Duration = Duration.between(start, end)
}
