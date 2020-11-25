package com.kylecorry.trailsensecore.domain.water

import java.time.Duration

class WaterService {

    fun getPurificationTime(altitude: Float?): Duration {
        if (altitude == null || altitude >= 1000f){
            return Duration.ofMinutes(3)
        }

        return Duration.ofMinutes(1)
    }

}