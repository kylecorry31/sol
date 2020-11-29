package com.kylecorry.trailsensecore.infrastructure.sensors.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.content.getSystemService
import com.kylecorry.trailsensecore.domain.Accuracy
import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.infrastructure.sensors.AbstractSensor
import com.kylecorry.trailsensecore.infrastructure.sensors.SensorChecker
import java.time.Instant

class GPS(context: Context) : AbstractSensor(), IGPS {

    override val hasValidReading: Boolean
        get() = hadRecentValidReading()

    override val satellites: Int
        get() = _satellites

    override val accuracy: Accuracy
        get() = _accuracy

    override val horizontalAccuracy: Float?
        get() = _horizontalAccuracy

    override val verticalAccuracy: Float?
        get() = _verticalAccuracy

    override val location: Coordinate
        get() = _location

    override val speed: Float
        get() = _speed

    override val altitude: Float
        get() = _altitude

    override val time: Instant
        get() = _time

    private val locationManager = context.getSystemService<LocationManager>()
    private val sensorChecker = SensorChecker(context)
    private val locationListener = SimpleLocationListener { updateLastLocation(it, true) }

    private var _altitude = 0f
    private var _time = Instant.now()
    private var _accuracy: Accuracy = Accuracy.Unknown
    private var _horizontalAccuracy: Float? = null
    private var _verticalAccuracy: Float? = null
    private var _satellites: Int = 0
    private var _speed: Float = 0f
    private var _location = Coordinate.zero

    private var lastLocation: Location? = null
    private var lastUpdate = 0L

    private var fixStart: Long = 0L
    private val maxFixTime = 8000L

    @SuppressLint("MissingPermission")
    override fun startImpl() {
        if (!sensorChecker.hasGPS()) {
            return
        }

        fixStart = System.currentTimeMillis()

        if (lastLocation == null) {
            updateLastLocation(
                locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER),
                false
            )
        }

        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            20,
            0f,
            locationListener
        )
    }

    override fun stopImpl() {
        locationManager?.removeUpdates(locationListener)
    }

    private fun updateLastLocation(location: Location?, notify: Boolean = true) {
        if (location == null) {
            return
        }

        _time = Instant.ofEpochMilli(location.time)

        val satellites = location.extras.getInt("satellites")
        val dt = System.currentTimeMillis() - fixStart

        if (useNewLocation(lastLocation, location) &&
            location.hasAltitude() && location.altitude != 0.0
        ) {
            // Forces an altitude update irrespective of the satellite count - helps when the GPS is being polled in the background
            _altitude = location.altitude.toFloat()
        }

        if (satellites < 4 && dt < maxFixTime) {
            return
        }

        if (!useNewLocation(lastLocation, location)) {
            if (notify) notifyListeners()
            return
        }

        fixStart = System.currentTimeMillis()
        lastUpdate = fixStart
        _satellites = satellites
        lastLocation = location

        if (location.hasAccuracy()) {
            this._accuracy = when {
                location.accuracy < 8 -> Accuracy.High
                location.accuracy < 16 -> Accuracy.Medium
                else -> Accuracy.Low
            }
            this._horizontalAccuracy = location.accuracy
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (location.hasVerticalAccuracy()) {
                this._verticalAccuracy = location.verticalAccuracyMeters
            }
        }

        if (location.hasSpeed()) {
            _speed =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && location.hasSpeedAccuracy()) {
                    if (location.speed < location.speedAccuracyMetersPerSecond * 0.68) {
                        0f
                    } else {
                        location.speed
                    }

                } else {
                    location.speed
                }
        }

        this._location = Coordinate(
            location.latitude,
            location.longitude
        )

        if (location.hasAltitude() && location.altitude != 0.0) {
            _altitude = location.altitude.toFloat()
        }

        if (notify) notifyListeners()
    }

    private fun hadRecentValidReading(): Boolean {
        val now = System.currentTimeMillis()
        val recentThreshold = 1000 * 60 * 2L
        return now - lastUpdate <= recentThreshold
    }

    private fun useNewLocation(current: Location?, newLocation: Location): Boolean {
        // Modified from https://stackoverflow.com/questions/10588982/retrieving-of-satellites-used-in-gps-fix-from-android
        if (current == null) {
            return true
        }

        val timeDelta = newLocation.time - current.time
        val isSignificantlyNewer: Boolean = timeDelta > 1000 * 60 * 2
        val isSignificantlyOlder: Boolean = timeDelta < -1000 * 60 * 2
        val isNewer = timeDelta > 0

        if (isSignificantlyNewer) {
            return true
        } else if (isSignificantlyOlder) {
            return false
        }

        val accuracyDelta = (newLocation.accuracy - current.accuracy).toInt()
        val isMoreAccurate = accuracyDelta < 0
        val isSignificantlyLessAccurate = accuracyDelta > 30

        if (isMoreAccurate) {
            return true
        }

        return isNewer && !isSignificantlyLessAccurate
    }
}