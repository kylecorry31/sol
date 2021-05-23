package com.kylecorry.trailsensecore.infrastructure.sensors.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.trailsensecore.domain.math.Quaternion
import com.kylecorry.trailsensecore.domain.math.QuaternionMath
import com.kylecorry.trailsensecore.infrastructure.sensors.BaseSensor

class GameRotationSensor(context: Context) :
    BaseSensor(context, Sensor.TYPE_GAME_ROTATION_VECTOR, SensorManager.SENSOR_DELAY_FASTEST),
    IOrientationSensor {

    private val lock = Object()

    override val hasValidReading: Boolean
        get() = _hasReading

    override val orientation: Quaternion
        get() = Quaternion.from(rawOrientation)

    override val rawOrientation: FloatArray
        get() {
            return synchronized(lock) {
                _quaternion
            }
        }

    private val _quaternion = Quaternion.zero.toFloatArray()

    private var _hasReading = false

    override fun handleSensorEvent(event: SensorEvent) {
        synchronized(lock) {
            event.values.copyInto(_quaternion)
            QuaternionMath.inverse(_quaternion, _quaternion)
        }
        _hasReading = true
    }
}