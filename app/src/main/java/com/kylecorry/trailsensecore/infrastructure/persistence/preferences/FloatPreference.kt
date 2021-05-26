package com.kylecorry.trailsensecore.infrastructure.persistence.preferences

import com.kylecorry.trailsensecore.infrastructure.persistence.Cache
import kotlin.reflect.KProperty

class FloatPreference(
    private val cache: Cache,
    private val name: String,
    private val defaultValue: Float
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return cache.getFloat(name) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        cache.putFloat(name, value)
    }

}