package com.kylecorry.trailsensecore.domain.navigation

import android.location.Location
import com.kylecorry.trailsensecore.domain.Bearing
import com.kylecorry.trailsensecore.domain.Coordinate
import com.kylecorry.trailsensecore.domain.math.clamp
import java.time.Duration
import kotlin.math.PI
import kotlin.math.max

class NavigationService : INavigationService {
    override fun navigate(
        from: Coordinate,
        to: Coordinate,
        declination: Float,
        useTrueNorth: Boolean
    ): NavigationVector {
        val results = FloatArray(3)
        Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, results)

        val declinationAdjustment = if (useTrueNorth) {
            0f
        } else {
            -declination
        }

        return NavigationVector(
            Bearing(results[1]).withDeclination(declinationAdjustment),
            results[0]
        )
    }

    override fun navigate(
        from: Position,
        to: Beacon,
        declination: Float,
        usingTrueNorth: Boolean
    ): NavigationVector {
        val originalVector = navigate(from.location, to.coordinate, declination, usingTrueNorth)
        val altitudeChange = if (to.elevation != null) to.elevation - from.altitude else null
        return originalVector.copy(altitudeChange = altitudeChange)
    }

    override fun eta(from: Position, to: Beacon, nonLinear: Boolean): Duration {
        val speed =
            if (from.speed < 3) clamp(from.speed, 0.89408f, 1.78816f) else from.speed
        val elevationGain =
            max(if (to.elevation == null) 0f else (to.elevation - from.altitude), 0f)
        val distance =
            from.location.distanceTo(to.coordinate) * (if (nonLinear) PI.toFloat() / 2f else 1f)

        val baseTime = distance / speed
        val elevationMinutes = (elevationGain / 300f) * 30f * 60f

        return Duration.ofSeconds(baseTime.toLong()).plusSeconds(elevationMinutes.toLong())
    }
}