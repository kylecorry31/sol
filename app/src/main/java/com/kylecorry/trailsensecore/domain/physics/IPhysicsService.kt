package com.kylecorry.trailsensecore.domain.physics

import com.kylecorry.andromeda.core.units.Distance
import java.time.Duration

interface IPhysicsService {
    fun fallHeight(time: Duration): Distance
}