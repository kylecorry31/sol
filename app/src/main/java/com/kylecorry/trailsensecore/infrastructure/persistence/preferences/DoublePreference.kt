package com.kylecorry.trailsensecore.infrastructure.persistence.preferences

import com.kylecorry.trailsensecore.infrastructure.persistence.Cache
import kotlin.reflect.KProperty

class DoublePreference(
    private val cache: Cache,
    private val name: String,
    private val defaultValue: Double
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Double {
        return cache.getDouble(name) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
        cache.putDouble(name, value)
    }

}