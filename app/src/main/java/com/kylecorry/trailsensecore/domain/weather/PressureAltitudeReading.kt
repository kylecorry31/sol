package com.kylecorry.trailsensecore.domain.weather

import java.time.Instant

data class PressureAltitudeReading(val time: Instant, val pressure: Float, val altitude: Float, val temperature: Float)