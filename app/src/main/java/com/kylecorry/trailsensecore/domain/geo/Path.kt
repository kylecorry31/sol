package com.kylecorry.trailsensecore.domain.geo

import androidx.annotation.ColorInt
import com.kylecorry.andromeda.core.units.Distance
import java.time.Duration
import java.time.Instant

data class Path(
    val id: Long,
    val name: String,
    val points: List<PathPoint>,
    @ColorInt val color: Int,
    val style: PathStyle = PathStyle.Solid,
    val deleteAfter: Duration? = null,
    val owner: PathOwner = PathOwner.User,
    val visible: Boolean = true
) {
    val distance: Distance
        get() {
            if (points.size < 2) {
                return Distance.meters(0f)
            }

            var distance = 0f
            for (i in 0 until points.lastIndex) {
                distance += points[i].coordinate.distanceTo(points[i + 1].coordinate)
            }

            return Distance.meters(distance)
        }

    val lastPoint = points.lastOrNull()

    val duration: Duration?
        get() {
            val start = points.firstOrNull()?.time ?: return null
            val end = lastPoint?.time ?: return null

            return Duration.between(start, end)
        }

    val deleteOn: Instant?
        get() {
            deleteAfter ?: return null
            return lastPoint?.time?.plus(deleteAfter)
        }
}