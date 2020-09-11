package com.kylecorry.trailsensecore.domain

import com.kylecorry.trailsensecore.domain.math.normalizeAngle
import kotlin.math.roundToInt

class Bearing(_value: Float){
    val value: Float =
        normalizeAngle(_value)

    val direction: CompassDirection
            get(){
                val directions = CompassDirection.values()
                val a = ((value / 45f).roundToInt() * 45f) % 360
                directions.forEach {
                    if (a == it.azimuth){
                        return it
                    }
                }
                return CompassDirection.North
            }

    fun withDeclination(declination: Float): Bearing {
        return Bearing(value + declination)
    }
}