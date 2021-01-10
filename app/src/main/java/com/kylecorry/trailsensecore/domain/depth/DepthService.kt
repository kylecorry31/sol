package com.kylecorry.trailsensecore.domain.depth

import com.kylecorry.trailsensecore.domain.units.Distance
import com.kylecorry.trailsensecore.domain.units.DistanceUnits
import com.kylecorry.trailsensecore.domain.units.Pressure
import com.kylecorry.trailsensecore.domain.units.PressureUnits

class DepthService {

    fun calculateDepth(pressure: Pressure, seaLevelPressure: Pressure): Distance {
        if (pressure <= seaLevelPressure){
            return Distance(0f, DistanceUnits.Meters)
        }

        val waterDensity = 1030f
        val gravity = 9.81f
        val pressureDiff = pressure.convertTo(PressureUnits.Hpa).pressure - seaLevelPressure.convertTo(PressureUnits.Hpa).pressure

        return Distance(pressureDiff * 100 / (gravity * waterDensity), DistanceUnits.Meters)
    }

}