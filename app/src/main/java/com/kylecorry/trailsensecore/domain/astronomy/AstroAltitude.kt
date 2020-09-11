package com.kylecorry.trailsensecore.domain.astronomy

import java.time.LocalDateTime

data class AstroAltitude(val time: LocalDateTime, val altitudeDegrees: Float){
    val altitudeRadians = Math.toRadians(altitudeDegrees.toDouble()).toFloat()
}