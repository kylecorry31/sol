package com.kylecorry.trailsensecore.infrastructure.persistence

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.kylecorry.trailsensecore.domain.math.toDoubleCompat

class Cache(context: Context) {

    private val sharedPrefs by lazy { PreferenceManager.getDefaultSharedPreferences(context.applicationContext) }

    fun remove(key: String) {
        sharedPrefs?.edit { remove(key) }
    }

    fun contains(key: String): Boolean {
        return sharedPrefs?.contains(key) ?: false
    }

    fun putInt(key: String, value: Int) {
        sharedPrefs?.edit { putInt(key, value) }
    }

    fun putBoolean(key: String, value: Boolean) {
        sharedPrefs?.edit { putBoolean(key, value) }
    }

    fun putString(key: String, value: String) {
        sharedPrefs?.edit { putString(key, value) }
    }

    fun putFloat(key: String, value: Float) {
        sharedPrefs?.edit { putFloat(key, value) }
    }

    fun putDouble(key: String, value: Double) {
        sharedPrefs?.edit { putString(key, value.toString()) }
    }

    fun putLong(key: String, value: Long) {
        sharedPrefs?.edit { putLong(key, value) }
    }

    fun getInt(key: String): Int? {
        if (!contains(key)) {
            return null
        }
        return sharedPrefs?.getInt(key, 0)
    }

    fun getBoolean(key: String): Boolean? {
        if (!contains(key)) {
            return null
        }
        return sharedPrefs?.getBoolean(key, false)
    }

    fun getString(key: String): String? {
        if (!contains(key)) {
            return null
        }
        return sharedPrefs?.getString(key, null)
    }

    fun getFloat(key: String): Float? {
        if (!contains(key)) {
            return null
        }
        return sharedPrefs?.getFloat(key, 0f)
    }

    fun getDouble(key: String): Double? {
        if (!contains(key)) {
            return null
        }
        return sharedPrefs?.getString(key, null)?.toDoubleCompat()
    }

    fun getLong(key: String): Long? {
        if (!contains(key)) {
            return null
        }
        return sharedPrefs?.getLong(key, 0L)
    }
}