package com.kylecorry.trailsensecore.infrastructure.sensors.accelerometer

import android.content.Context
import com.kylecorry.trailsensecore.infrastructure.sensors.AbstractSensor
import com.kylecorry.trailsensecore.infrastructure.sensors.SensorChecker
import kotlin.math.abs
import kotlin.math.withSign

class DeviceOrientation(private val context: Context) : AbstractSensor() {
    override fun startImpl() {
        accelerometer.start(this::onAccelerometer)
    }

    override fun stopImpl() {
        accelerometer.stop(this::onAccelerometer)
    }

    override val hasValidReading: Boolean
        get() = gotReading

    var orientation: Orientation = Orientation.Flat
        private set

    private val sensorChecker by lazy { SensorChecker(context) }

    private val accelerometer: IAccelerometer by lazy {
        if (sensorChecker.hasGravity()) GravitySensor(context) else LowPassAccelerometer(context)
    }

    private var gotReading = false

    private fun onAccelerometer(): Boolean {
        val acceleration = accelerometer.acceleration.toFloatArray()
        var largestAccelAxis = 0
        for (i in acceleration.indices) {
            if (abs(acceleration[i]) > abs(acceleration[largestAccelAxis])) {
                largestAccelAxis = i
            }
        }

        largestAccelAxis = (largestAccelAxis + 1).toDouble()
            .withSign(acceleration[largestAccelAxis].toDouble()).toInt()

        orientation = when (largestAccelAxis) {
            -3 -> Orientation.FlatInverse
            -2 -> Orientation.PortraitInverse
            -1 -> Orientation.LandscapeInverse
            1 -> Orientation.Landscape
            2 -> Orientation.Portrait
            else -> Orientation.Flat
        }

        gotReading = true

        return true
    }

    enum class Orientation {
        Portrait,
        PortraitInverse,
        Flat,
        FlatInverse,
        Landscape,
        LandscapeInverse
    }

}