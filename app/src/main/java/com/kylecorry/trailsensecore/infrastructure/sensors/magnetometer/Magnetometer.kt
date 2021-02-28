package com.kylecorry.trailsensecore.infrastructure.sensors.magnetometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.trailsensecore.domain.math.Vector3
import com.kylecorry.trailsensecore.infrastructure.sensors.BaseSensor

class Magnetometer(context: Context): BaseSensor(context, Sensor.TYPE_MAGNETIC_FIELD, SensorManager.SENSOR_DELAY_FASTEST),
    IMagnetometer {

    override val hasValidReading: Boolean
        get() = gotReading
    private var gotReading = false

    override val magneticField
        get() = _magField

    private var _magField = Vector3.zero

    override fun handleSensorEvent(event: SensorEvent) {
        _magField = Vector3(
            event.values[0],
            event.values[1],
            event.values[2]
        )
        gotReading = true
    }

}