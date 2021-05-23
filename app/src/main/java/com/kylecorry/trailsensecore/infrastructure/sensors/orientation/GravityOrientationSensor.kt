package com.kylecorry.trailsensecore.infrastructure.sensors.orientation

import android.content.Context
import com.kylecorry.trailsensecore.domain.math.*
import com.kylecorry.trailsensecore.domain.units.Quality
import com.kylecorry.trailsensecore.infrastructure.sensors.AbstractSensor
import com.kylecorry.trailsensecore.infrastructure.sensors.SensorChecker
import com.kylecorry.trailsensecore.infrastructure.sensors.accelerometer.GravitySensor
import com.kylecorry.trailsensecore.infrastructure.sensors.accelerometer.LowPassAccelerometer
import com.kylecorry.trailsensecore.infrastructure.sensors.accelerometer.IAccelerometer
import kotlin.math.atan2
import kotlin.math.sqrt

// Algorithm from https://www.digikey.com/en/articles/using-an-accelerometer-for-inclination-sensing
class GravityOrientationSensor(context: Context) : AbstractSensor(), IOrientationSensor {

    override val hasValidReading: Boolean
        get() = gotReading

    private var gotReading = false

    override val quality: Quality
        get() = _quality

    private val lock = Object()

    override val orientation: Quaternion
        get() = Quaternion.from(rawOrientation)

    override val rawOrientation: FloatArray
        get() = synchronized(lock){
            _quaternion.clone()
        }

    private val _quaternion = Quaternion.zero.toFloatArray()

    private var _quality = Quality.Unknown

    private val sensorChecker = SensorChecker(context)
    private val accelerometer: IAccelerometer =
        if (sensorChecker.hasGravity()) GravitySensor(context) else LowPassAccelerometer(context)

    private fun updateSensor(): Boolean {

        // Gravity
        val gravity = accelerometer.rawAcceleration

        val roll = atan2(gravity[0], sqrt(gravity[1] * gravity[1] + gravity[2] * gravity[2])).toDegrees()
        val pitch = -atan2(gravity[1], sqrt(gravity[0] * gravity[0] + gravity[2] * gravity[2])).toDegrees()

        synchronized(lock) {
            QuaternionMath.fromEuler(floatArrayOf(roll, pitch, 0f), _quaternion)
        }

        _quality = accelerometer.quality

        gotReading = true
        notifyListeners()
        return true
    }

    override fun startImpl() {
        accelerometer.start(this::updateSensor)
    }

    override fun stopImpl() {
        accelerometer.stop(this::updateSensor)
    }

}