package com.kylecorry.trailsensecore.domain.light

import com.kylecorry.andromeda.core.units.Distance
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sqrt

class LightService {

    fun toCandela(lux: Float, distance: Distance): Float {
        val meters = distance.meters().distance
        return lux * meters * meters
    }

    fun luxAtDistance(candela: Float, distance: Distance): Float {
        val meters = distance.meters().distance
        return candela / (meters * meters)
    }

    fun beamDistance(candela: Float): Distance {
        return Distance.meters(sqrt(candela * 4))
    }

    fun describeLux(lux: Float): LightIntensity {
        val lnLux = ln(lux)
        var nearest = LightIntensity.NoMoon

        LightIntensity.values().forEach {
            val diff = abs(lnLux - ln(it.lux))
            val minDiff = abs(lnLux - ln(nearest.lux))
            if (diff < minDiff){
                nearest = it
            }
        }

        return nearest
    }
}