package com.kylecorry.trailsensecore.domain.astronomy.eclipse

import java.time.Duration
import java.time.Instant

data class Eclipse(val start: Instant, val end: Instant, val magnitude: Float) {
    val maximum: Instant
        get() {
            return start.plus(Duration.between(start, end).dividedBy(2))
        }
}
