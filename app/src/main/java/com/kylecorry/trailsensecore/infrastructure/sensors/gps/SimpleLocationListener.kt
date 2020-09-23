package com.kylecorry.trailsensecore.infrastructure.sensors.gps

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

internal class SimpleLocationListener(private val onLocationChangedFn: (location: Location?) -> Unit): LocationListener {
    override fun onLocationChanged(location: Location) {
        onLocationChangedFn.invoke(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }
}