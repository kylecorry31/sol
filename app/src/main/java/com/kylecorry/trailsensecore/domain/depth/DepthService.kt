package com.kylecorry.trailsensecore.domain.depth

class DepthService {

    fun calculateDepth(pressure: Float, seaLevelPressure: Float): Float {
        if (pressure <= seaLevelPressure){
            return 0f
        }

        val waterDensity = 1030f
        val gravity = 9.81f
        return (pressure - seaLevelPressure) * 100 / (gravity * waterDensity)
    }

}