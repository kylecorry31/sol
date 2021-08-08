package com.kylecorry.trailsensecore.infrastructure.sensors.altimeter

import android.hardware.SensorManager
import com.kylecorry.sense.AbstractSensor
import com.kylecorry.trailsensecore.infrastructure.sensors.barometer.IBarometer

class BarometricAltimeter(private val barometer: IBarometer, private val seaLevelPressureFn: () -> Float) : AbstractSensor(),
    IAltimeter {

    override val altitude: Float
        get() = SensorManager.getAltitude(seaLevelPressureFn.invoke(), barometer.pressure)

    override val hasValidReading: Boolean
        get() = barometer.hasValidReading

    private fun onBarometerUpdate(): Boolean {
        notifyListeners()
        return true
    }

    override fun startImpl() {
        barometer.start(this::onBarometerUpdate)
    }

    override fun stopImpl() {
        barometer.stop(this::onBarometerUpdate)
    }
}