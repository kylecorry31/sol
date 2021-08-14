package com.kylecorry.trailsensecore.infrastructure.sensors

import android.content.Context
import android.hardware.Sensor
import com.kylecorry.andromeda.location.GPS
import com.kylecorry.andromeda.permissions.PermissionService
import com.kylecorry.andromeda.sense.SensorChecker
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils

class SensorDetailProvider {

    fun getSensorDetails(context: Context): String {
        val permissions = PermissionService(context)
        val sensorChecker = SensorChecker(context)
        val locationPermission = permissions.canGetFineLocation()
        val backgroundLocationPermission = permissions.isBackgroundLocationEnabled()
        val gps = GPS.isAvailable(context)
        val barometer = sensorChecker.hasBarometer()
        val gravity =  sensorChecker.hasGravity()
        val hygrometer = sensorChecker.hasHygrometer()
        val magnetometer = sensorChecker.hasSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val accelerometer = sensorChecker.hasSensor(Sensor.TYPE_ACCELEROMETER)
        val orientation = sensorChecker.hasSensor(Sensor.TYPE_ORIENTATION)

        return "Location: $locationPermission\nBackground Location: $backgroundLocationPermission\nGPS: $gps\nBarometer: $barometer\nGravity: $gravity\nHygrometer: $hygrometer\nMagnetometer: $magnetometer\nAccelerometer: $accelerometer\nOrientation: $orientation"
    }


}