package com.kylecorry.trailsensecore.domain.geo

import android.graphics.Color
import androidx.annotation.ColorInt
import com.kylecorry.andromeda.core.units.Distance
import java.time.Duration
import java.time.Instant

data class Path(
    val id: Long,
    val name: String,
    val deleteAfter: Duration? = null,
    val owner: PathOwner = PathOwner.User,
    // Display metadata
    val visible: Boolean = true,
    @ColorInt val color: Int = Color.BLACK,
    val style: PathStyle = PathStyle.Solid,
    // Point metadata
    val pointCount: Int = 0,
    val distance: Distance = Distance.meters(0f),
    val startTime: Instant? = null,
    val endTime: Instant? = null
) {
    val duration: Duration?
        get() {
            val start = startTime ?: return null
            val end = endTime ?: return null

            return Duration.between(start, end)
        }

    val deleteOn: Instant?
        get() {
            deleteAfter ?: return null
            return endTime?.plus(deleteAfter)
        }

    val isTemporary = deleteAfter != null
}