package com.kylecorry.trailsensecore.infrastructure.system

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.location.LocationManager
import androidx.core.content.getSystemService
import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.domain.units.Distance
import java.time.Duration

object GeofenceUtils {

    @SuppressLint("MissingPermission")
    fun addGeofence(
        context: Context,
        location: Coordinate,
        radius: Distance,
        pendingIntent: PendingIntent,
        expiration: Duration? = null
    ) {
        if (PermissionUtils.isLocationEnabled(context)) {
            context.getSystemService<LocationManager>()?.addProximityAlert(
                location.latitude,
                location.longitude,
                radius.meters().distance,
                expiration?.toMillis() ?: -1,
                pendingIntent
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun removeGeofence(context: Context, pendingIntent: PendingIntent) {
        if (PermissionUtils.isLocationEnabled(context)) {
            context.getSystemService<LocationManager>()?.removeProximityAlert(pendingIntent)
        }
    }

}