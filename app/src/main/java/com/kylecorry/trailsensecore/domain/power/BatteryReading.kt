package com.kylecorry.trailsensecore.domain.power

import java.time.Instant

data class BatteryReading(val time: Instant, val percent: Float, val capacity: Float, val isCharging: Boolean)