package com.kylecorry.trailsensecore.domain.physics

import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.andromeda.core.units.DistanceUnits
import java.time.Duration

class PhysicsService : IPhysicsService {

    override fun fallHeight(time: Duration): Distance {
        val seconds = time.toMillis() / 1000f
        return Distance(0.5f * GRAVITY * seconds * seconds, DistanceUnits.Meters)
    }

    companion object {
        const val GRAVITY = 9.81f
    }

}