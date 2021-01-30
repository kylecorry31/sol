package com.kylecorry.trailsensecore.domain.health.heart

import com.kylecorry.trailsensecore.domain.health.heart.BloodPressure
import java.time.Instant

data class BloodPressureReading(val id: Long, val pressure: BloodPressure, val time: Instant)
