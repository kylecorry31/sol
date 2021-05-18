package com.kylecorry.trailsensecore.infrastructure.sensors.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.trailsensecore.domain.math.*
import com.kylecorry.trailsensecore.infrastructure.sensors.BaseSensor
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Gyroscope(context: Context, private val threshold: Float = 0.00001f) :
    BaseSensor(context, Sensor.TYPE_GYROSCOPE, SensorManager.SENSOR_DELAY_FASTEST),
    IGyroscope {

    override val rawEuler: FloatArray
        get() {
            return synchronized(lock) {
                val euler = FloatArray(3)
                QuaternionMath.toEuler(_quaternion, euler)
                euler
            }
        }

    override val euler: Euler
        get() = Euler.from(rawEuler)

    override val quaternion: Quaternion
        get() = Quaternion.from(rawQuaternion)

    override val rawQuaternion: FloatArray
        get() {
            return synchronized(lock) {
                _quaternion.clone()
            }
        }

    private val _quaternion = Quaternion.zero.toFloatArray()

    private val NS2S = 1.0f / 1000000000.0f

    override val hasValidReading: Boolean
        get() = _hasReading

    private var _hasReading = false
    private var lastTime = 0L

    private val deltaRotationVector = FloatArray(4)

    private val lock = Object()

    override fun handleSensorEvent(event: SensorEvent) {
        if (event.values.size < 3) {
            return
        }

        if (lastTime == 0L) {
            lastTime = event.timestamp
            return
        }
        val dt = (event.timestamp - lastTime) * NS2S
        lastTime = event.timestamp


        var axisX = -event.values[0]
        var axisY = -event.values[1]
        var axisZ = -event.values[2]

        val omegaMagnitude = sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ)

        if (omegaMagnitude > threshold) {
            axisX /= omegaMagnitude
            axisY /= omegaMagnitude
            axisZ /= omegaMagnitude
        }

        val thetaOverTwo = omegaMagnitude * dt / 2.0f
        val sinThetaOverTwo = sin(thetaOverTwo)
        val cosThetaOverTwo = cos(thetaOverTwo)

        synchronized(lock) {
            deltaRotationVector[0] = sinThetaOverTwo * axisX
            deltaRotationVector[1] = sinThetaOverTwo * axisY
            deltaRotationVector[2] = sinThetaOverTwo * axisZ
            deltaRotationVector[3] = cosThetaOverTwo
            QuaternionMath.multiply(_quaternion, deltaRotationVector, _quaternion)
            QuaternionMath.normalize(_quaternion, _quaternion)
        }

        _hasReading = true
    }


    override fun calibrate() {
        synchronized(lock) {
            _quaternion[0] = 0f
            _quaternion[1] = 0f
            _quaternion[2] = 0f
            _quaternion[3] = 1f
        }
    }


}