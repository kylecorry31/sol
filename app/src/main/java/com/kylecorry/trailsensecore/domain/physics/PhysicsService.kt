package com.kylecorry.trailsensecore.domain.physics

import com.kylecorry.trailsensecore.domain.units.Distance
import com.kylecorry.trailsensecore.domain.units.DistanceUnits
import java.time.Duration
import kotlin.math.pow

class PhysicsService : IPhysicsService {

    override fun fallHeight(time: Duration): Distance {
        val seconds = time.toMillis() / 1000f
        return Distance(0.5f * GRAVITY * seconds * seconds, DistanceUnits.Meters)
    }

    companion object {
        const val GRAVITY = 9.81f
    }

}