package com.kylecorry.trailsensecore.infrastructure.persistence.preferences

import com.kylecorry.trailsensecore.infrastructure.persistence.Cache
import kotlin.reflect.KProperty

class IntPreference(
    private val cache: Cache,
    private val name: String,
    private val defaultValue: Int
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return cache.getInt(name) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        cache.putInt(name, value)
    }

}