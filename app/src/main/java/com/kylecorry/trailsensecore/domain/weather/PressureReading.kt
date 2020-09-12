package com.kylecorry.trailsensecore.domain.weather

import java.time.Instant

data class PressureReading(val time: Instant, val value: Float)