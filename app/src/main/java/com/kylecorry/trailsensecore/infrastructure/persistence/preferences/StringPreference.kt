package com.kylecorry.trailsensecore.infrastructure.persistence.preferences

import com.kylecorry.trailsensecore.infrastructure.persistence.Cache
import kotlin.reflect.KProperty

class StringPreference(
    private val cache: Cache,
    private val name: String,
    private val defaultValue: String
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return cache.getString(name) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        cache.putString(name, value)
    }

}