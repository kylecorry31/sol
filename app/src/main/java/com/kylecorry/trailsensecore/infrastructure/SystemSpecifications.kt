package com.kylecorry.trailsensecore.infrastructure

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.os.Build
import androidx.core.content.getSystemService
import com.kylecorry.trailsensecore.domain.specifications.Specification
import com.kylecorry.trailsensecore.infrastructure.persistence.Cache
import com.kylecorry.trailsensecore.infrastructure.sensors.SensorChecker
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils

class HasSensor(private val type: Int) : Specification<Context>() {
    override fun isSatisfiedBy(context: Context): Boolean {
        return SensorChecker(context).hasSensor(type)
    }
}

class HasSensorLike(private val name: String) : Specification<Context>() {
    override fun isSatisfiedBy(context: Context): Boolean {
        return SensorChecker(context).hasSensorLike(name)
    }
}

class LocationIsEnabled() : Specification<Context>() {
    override fun isSatisfiedBy(context: Context): Boolean {
        if (HasPermission(Manifest.permission.ACCESS_FINE_LOCATION).not().isSatisfiedBy(context)) {
            return false
        }
        val lm = context.getSystemService<LocationManager>()
        try {
            return lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        } catch (e: Exception) {
            // Do nothing
        }
        return false
    }
}

class HasPermission(private val permission: String): Specification<Context>() {
    override fun isSatisfiedBy(context: Context): Boolean {
        return PermissionUtils.hasPermission(context, permission)
    }
}

class IsPreferenceOn(private val id: String, private val defaultValue: Boolean = false): Specification<Context>() {
    override fun isSatisfiedBy(context: Context): Boolean {
        val cache = Cache(context)
        return cache.getBoolean(id) ?: defaultValue
    }
}

class VersionIsAtLeast<T>(private val versionCode: Int): Specification<T>() {
    override fun isSatisfiedBy(nothing: T): Boolean {
        return Build.VERSION.SDK_INT >= versionCode
    }
}

class VersionIsAtMost<T>(private val versionCode: Int): Specification<T>() {
    override fun isSatisfiedBy(nothing: T): Boolean {
        return Build.VERSION.SDK_INT <= versionCode
    }
}