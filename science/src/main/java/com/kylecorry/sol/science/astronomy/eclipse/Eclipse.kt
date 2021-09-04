package com.kylecorry.sol.science.astronomy.eclipse

import java.time.Duration
import java.time.Instant

data class Eclipse(val start: Instant, val end: Instant, val magnitude: Float) {
    val duration: Duration = Duration.between(start, end)
    val maximum: Instant = start.plus(duration.dividedBy(2))
}
