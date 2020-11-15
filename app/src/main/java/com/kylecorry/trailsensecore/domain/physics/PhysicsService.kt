package com.kylecorry.trailsensecore.domain.physics

import java.time.Duration
import kotlin.math.pow

class PhysicsService {

    fun fallHeight(time: Duration): Float {
        val seconds = time.toMillis() / 1000f
        return 0.5f * GRAVITY * seconds * seconds
    }

    companion object {
        const val GRAVITY = 9.81f
    }

}