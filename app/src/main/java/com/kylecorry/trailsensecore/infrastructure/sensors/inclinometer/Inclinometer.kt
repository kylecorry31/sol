package com.kylecorry.trailsensecore.infrastructure.sensors.inclinometer

import android.content.Context
import com.kylecorry.trailsensecore.domain.inclinometer.InclinationCalculator
import com.kylecorry.sense.Quality
import com.kylecorry.sense.AbstractSensor
import com.kylecorry.trailsensecore.infrastructure.sensors.SensorChecker
import com.kylecorry.trailsensecore.infrastructure.sensors.accelerometer.GravitySensor
import com.kylecorry.trailsensecore.infrastructure.sensors.accelerometer.LowPassAccelerometer
import com.kylecorry.trailsensecore.infrastructure.sensors.accelerometer.IAccelerometer

class Inclinometer(context: Context) : AbstractSensor(), IInclinometer {

    override val angle: Float
        get() = _angle

    override val hasValidReading: Boolean
        get() = gotReading

    private var gotReading = false

    override val quality: Quality
        get() = _quality
    private var _quality = Quality.Unknown

    private val sensorChecker = SensorChecker(context)
    private val accelerometer: IAccelerometer =
        if (sensorChecker.hasGravity()) GravitySensor(context) else LowPassAccelerometer(context)

    private var _angle = 0f

    private fun updateSensor(): Boolean {

        // Gravity
        val gravity = accelerometer.acceleration
        _quality = accelerometer.quality
        _angle = InclinationCalculator.calculate(gravity)

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