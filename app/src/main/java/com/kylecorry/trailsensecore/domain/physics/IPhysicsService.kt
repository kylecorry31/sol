package com.kylecorry.trailsensecore.domain.physics

import com.kylecorry.trailsensecore.domain.units.Distance
import java.time.Duration

interface IPhysicsService {
    fun fallHeight(time: Duration): Distance
}