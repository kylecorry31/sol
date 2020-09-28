package com.kylecorry.trailsensecore.infrastructure.sensors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.LocationManager
import androidx.core.content.getSystemService
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils

class SensorChecker(private val context: Context) {

    private val sensorManager by lazy { context.getSystemService<SensorManager>() }

    fun hasBarometer(): Boolean {
        return hasSensor(Sensor.TYPE_PRESSURE)
    }

    fun hasGPS(): Boolean {
        if (!PermissionUtils.isLocationEnabled(context)) {
            return false
        }

        val lm = context.getSystemService<LocationManager>()
        try {
            return lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        } catch (e: Exception) {
            // Do nothing
        }
        return false
    }

    fun hasGravity(): Boolean {
        return hasSensor(Sensor.TYPE_GRAVITY)
    }

    fun hasThermometer(): Boolean {
        // True because of battery sensor (maybe check to see if the battery has the thermometer)
        return true
    }

    fun hasHygrometer(): Boolean {
        return hasSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
    }

    fun hasSensor(sensorCode: Int): Boolean {
        val sensors = sensorManager?.getSensorList(sensorCode)
        return sensors?.isNotEmpty() ?: false
    }

    fun hasSensorLike(name: String): Boolean {
        val sensors = sensorManager?.getSensorList(Sensor.TYPE_ALL)
        return sensors?.any { it.name.contains(name, true) } ?: false
    }

}