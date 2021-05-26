package com.kylecorry.trailsensecore.infrastructure.persistence.preferences

import com.kylecorry.trailsensecore.infrastructure.persistence.Cache
import kotlin.reflect.KProperty

class IntEnumPreference<T>(
    private val cache: Cache,
    private val name: String,
    private val mappings: Map<Int, T>,
    private val defaultValue: T
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val raw = cache.getInt(name) ?: return defaultValue
        return mappings.getOrDefault(raw, defaultValue)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val key = mappings.entries.firstOrNull { it.value == value }?.key ?: return
        cache.putInt(name, key)
    }

}