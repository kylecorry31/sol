package com.kylecorry.trailsensecore.infrastructure.system

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.location.LocationManager
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.andromeda.permissions.PermissionService
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
        val permissions = PermissionService(context)
        if (permissions.canGetFineLocation()) {
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
        val permissions = PermissionService(context)
        if (permissions.canGetFineLocation()) {
            context.getSystemService<LocationManager>()?.removeProximityAlert(pendingIntent)
        }
    }

}