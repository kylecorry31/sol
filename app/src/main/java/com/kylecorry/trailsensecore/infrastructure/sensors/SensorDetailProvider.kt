package com.kylecorry.trailsensecore.infrastructure.sensors

import android.content.Context
import android.hardware.Sensor
import com.kylecorry.andromeda.location.GPS
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.andromeda.sense.Sensors

class SensorDetailProvider {

    @Suppress("DEPRECATION")
    fun getSensorDetails(context: Context): String {
        val locationPermission = Permissions.canGetFineLocation(context)
        val backgroundLocationPermission = Permissions.isBackgroundLocationEnabled(context)
        val gps = GPS.isAvailable(context)
        val barometer = Sensors.hasBarometer(context)
        val gravity =  Sensors.hasGravity(context)
        val hygrometer = Sensors.hasHygrometer(context)
        val magnetometer = Sensors.hasSensor(context, Sensor.TYPE_MAGNETIC_FIELD)
        val accelerometer = Sensors.hasSensor(context, Sensor.TYPE_ACCELEROMETER)
        val orientation = Sensors.hasSensor(context, Sensor.TYPE_ORIENTATION)

        return "Location: $locationPermission\nBackground Location: $backgroundLocationPermission\nGPS: $gps\nBarometer: $barometer\nGravity: $gravity\nHygrometer: $hygrometer\nMagnetometer: $magnetometer\nAccelerometer: $accelerometer\nOrientation: $orientation"
    }


}