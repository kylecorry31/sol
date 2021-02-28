package com.kylecorry.trailsensecore.domain.health.heart

import java.time.Instant

data class BloodPressureReading(val id: Long, val pressure: BloodPressure, val time: Instant)
