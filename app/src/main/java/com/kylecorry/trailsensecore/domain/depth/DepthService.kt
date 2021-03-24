package com.kylecorry.trailsensecore.domain.depth

import com.kylecorry.trailsensecore.domain.units.Distance
import com.kylecorry.trailsensecore.domain.units.DistanceUnits
import com.kylecorry.trailsensecore.domain.units.Pressure
import com.kylecorry.trailsensecore.domain.units.PressureUnits

class DepthService {

    fun calculateDepth(pressure: Pressure, seaLevelPressure: Pressure, isSaltWater: Boolean = true): Distance {
        if (pressure <= seaLevelPressure){
            return Distance(0f, DistanceUnits.Meters)
        }

        val waterDensity = if (isSaltWater) 1023.6f else 997.0474f
        val gravity = 9.81f
        val pressureDiff = pressure.convertTo(PressureUnits.Hpa).pressure - seaLevelPressure.convertTo(PressureUnits.Hpa).pressure

        return Distance(pressureDiff * 100 / (gravity * waterDensity), DistanceUnits.Meters)
    }

}