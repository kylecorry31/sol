package com.kylecorry.trailsensecore.infrastructure.sensors.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.trailsensecore.domain.Bearing
import com.kylecorry.trailsensecore.domain.math.MovingAverageFilter
import com.kylecorry.trailsensecore.infrastructure.sensors.BaseSensor
import kotlin.math.abs
import kotlin.math.floor

@Suppress("DEPRECATION")
class LegacyCompass(context: Context, smoothingAmount: Int, private val useTrueNorth: Boolean) :
    BaseSensor(context, Sensor.TYPE_ORIENTATION, SensorManager.SENSOR_DELAY_FASTEST),
    ICompass {

    override val hasValidReading: Boolean
        get() = gotReading
    private var gotReading = false

    private var filterSize = smoothingAmount * 2
    private val filter = MovingAverageFilter(filterSize)

    override var declination = 0f

    override val bearing: Bearing
        get() {
            return if (useTrueNorth) {
                Bearing(_filteredBearing).withDeclination(declination)
            } else {
                Bearing(_filteredBearing)
            }
        }

    private var _bearing = 0f
    private var _filteredBearing = 0f

    override fun handleSensorEvent(event: SensorEvent) {
        _bearing += deltaAngle(_bearing, event.values[0])

        _filteredBearing = filter.filter(_bearing.toDouble()).toFloat()
        gotReading = true
    }

    private fun deltaAngle(angle1: Float, angle2: Float): Float {
        var delta = angle2 - angle1
        delta += 180
        delta -= floor(delta / 360) * 360
        delta -= 180
        if (abs(abs(delta) - 180) <= Float.MIN_VALUE) {
            delta = 180f
        }
        return delta
    }

}