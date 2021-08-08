package com.kylecorry.trailsensecore.infrastructure.sensors.orientation

import android.content.Context
import com.kylecorry.trailsensecore.domain.geo.Bearing
import com.kylecorry.trailsensecore.domain.geo.GeoService
import com.kylecorry.trailsensecore.domain.math.*
import com.kylecorry.sense.Quality
import com.kylecorry.sense.AbstractSensor
import com.kylecorry.trailsensecore.infrastructure.sensors.SensorChecker
import com.kylecorry.trailsensecore.infrastructure.sensors.accelerometer.GravitySensor
import com.kylecorry.trailsensecore.infrastructure.sensors.accelerometer.LowPassAccelerometer
import com.kylecorry.trailsensecore.infrastructure.sensors.accelerometer.IAccelerometer
import com.kylecorry.trailsensecore.infrastructure.sensors.compass.ICompass
import com.kylecorry.trailsensecore.infrastructure.sensors.magnetometer.LowPassMagnetometer
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

// Algorithm from https://www.digikey.com/en/articles/using-an-accelerometer-for-inclination-sensing
class OrientationSensor(context: Context, smoothingFactor: Int, private val useTrueNorth: Boolean) :
    AbstractSensor(), IOrientationSensor, ICompass {

    override val hasValidReading: Boolean
        get() = gotReading

    private var gotReading = false

    override val quality: Quality
        get() = _quality

    private val lock = Object()

    override val orientation: Quaternion
        get() = Quaternion.from(rawOrientation)

    override val rawOrientation: FloatArray
        get() = synchronized(lock) {
            _quaternion.clone()
        }

    private var filterSize = smoothingFactor * 2 * 2
    private val filter = MovingAverageFilter(max(1, filterSize))

    override var declination = 0f

    override val bearing: Bearing
        get() {
            return if (useTrueNorth) {
                Bearing(_filteredBearing).withDeclination(declination)
            } else {
                Bearing(_filteredBearing)
            }
        }

    override val rawBearing: Float
        get() {
            return if (useTrueNorth) {
                Bearing.getBearing(Bearing.getBearing(_filteredBearing) + declination)
            } else {
                Bearing.getBearing(_filteredBearing)
            }
        }

    private var _bearing = 0f
    private var _filteredBearing = 0f

    private val _quaternion = Quaternion.zero.toFloatArray()

    private var _quality = Quality.Unknown


    private val sensorChecker = SensorChecker(context)
    private val accelerometer: IAccelerometer =
        if (sensorChecker.hasGravity()) GravitySensor(context) else LowPassAccelerometer(context)
    private val magnetometer = LowPassMagnetometer(context)
    private val geoService = GeoService()

    private fun updateSensor(): Boolean {

        // Gravity
        val gravity = accelerometer.rawAcceleration

        val roll =
            atan2(gravity[0], sqrt(gravity[1] * gravity[1] + gravity[2] * gravity[2])).toDegrees()
        val pitch =
            -atan2(gravity[1], sqrt(gravity[0] * gravity[0] + gravity[2] * gravity[2])).toDegrees()
        val yaw = geoService.getAzimuth(gravity, magnetometer.rawMagneticField)?.value ?: 0f

        synchronized(lock) {
            QuaternionMath.fromEuler(floatArrayOf(roll, pitch, yaw), _quaternion)
            updateBearing(yaw)
        }

        val accelAccuracy = accelerometer.quality
        val magAccuracy = magnetometer.quality
        _quality = Quality.values()[min(accelAccuracy.ordinal, magAccuracy.ordinal)]

        gotReading = true
        notifyListeners()
        return true
    }

    private fun updateBearing(newBearing: Float) {
        _bearing += deltaAngle(_bearing, newBearing)
        _filteredBearing = filter.filter(_bearing.toDouble()).toFloat()
    }

    override fun startImpl() {
        accelerometer.start(this::updateSensor)
        magnetometer.start(this::updateSensor)
    }

    override fun stopImpl() {
        accelerometer.stop(this::updateSensor)
        magnetometer.stop(this::updateSensor)
    }

}