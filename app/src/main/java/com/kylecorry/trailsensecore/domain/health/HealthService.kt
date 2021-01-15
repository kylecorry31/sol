package com.kylecorry.trailsensecore.domain.health

import java.time.Duration

class HealthService : IHealthService {

    override fun getHeartRate(beats: Int, duration: Duration): Float {
        if (duration.isZero){
            return 0f
        }

        return beats / (duration.toMillis() / 1000f / 60f)
    }

}