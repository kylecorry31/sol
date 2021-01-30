package com.kylecorry.trailsensecore.infrastructure.sensors.gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.content.getSystemService
import com.kylecorry.trailsensecore.domain.Accuracy
import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.infrastructure.sensors.AbstractSensor
import com.kylecorry.trailsensecore.infrastructure.sensors.SensorChecker
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils
import java.time.Duration
import java.time.Instant


@SuppressLint("MissingPermission")
class AssistedGPS(private val context: Context) : AbstractSensor(), IGPS {

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

    override val time: Instant
        get() = _time

    override val altitude: Float
        get() = _altitude

    private val locationManager by lazy { context.getSystemService<LocationManager>() }
    private val sensorChecker by lazy { SensorChecker(context) }
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

    init {
        updateLastLocation(getLastLocation(), false)
    }

    private fun getLastLocation(): Location? {
        try {
            if (isGPSEnabled()) {
                val location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null) {
                    return location
                }
            }

            if (isNetworkEnabled()) {
                return locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
        } catch (e: Exception) {
            return null
        }

        return null
    }

    private fun isGPSEnabled(): Boolean {
        return PermissionUtils.isLocationEnabled(context)
                && locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    }

    private fun isNetworkEnabled(): Boolean {
        return PermissionUtils.hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                && locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    override fun startImpl() {
        if (!sensorChecker.hasGPS()) {
            return
        }

        if (lastLocation == null) {
            updateLastLocation(getLastLocation(), false)
        }

        if (isGPSEnabled()) {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                20,
                0f,
                locationListener
            )
        } else if (isNetworkEnabled()){
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                20,
                0f,
                locationListener
            )
        }
    }

    override fun stopImpl() {
        locationManager?.removeUpdates(locationListener)
    }

    private fun updateLastLocation(location: Location?, notify: Boolean = true) {
        location ?: return
        _location = Coordinate(location.latitude, location.longitude)
        _time = Instant.ofEpochMilli(location.time)
        _satellites =
            if (location.extras?.containsKey("satellites") == true) location.extras.getInt("satellites") else 0
        _altitude = if (location.hasAltitude()) location.altitude.toFloat() else 0f
        val accuracy = if (location.hasAccuracy()) location.accuracy else null
        _accuracy = when {
            accuracy != null && accuracy < 8 -> Accuracy.High
            accuracy != null && accuracy < 16 -> Accuracy.Medium
            accuracy != null -> Accuracy.Low
            else -> Accuracy.Unknown
        }
        _horizontalAccuracy = accuracy ?: 0f
        _verticalAccuracy =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && location.hasVerticalAccuracy()) {
                location.verticalAccuracyMeters
            } else {
                null
            }
        // TODO: Add speed accuracy to IGPS
        _speed = if (location.hasSpeed()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && location.hasSpeedAccuracy()) {
                if (location.speed < location.speedAccuracyMetersPerSecond * 0.68) {
                    0f
                } else {
                    location.speed
                }
            } else {
                location.speed
            }
        } else {
            0f
        }

        if (notify) notifyListeners()
    }

    private fun hadRecentValidReading(): Boolean {
        val last = time
        val now = Instant.now()
        val recentThreshold = Duration.ofMinutes(2)
        return Duration.between(last, now) <= recentThreshold && location != Coordinate.zero
    }
}