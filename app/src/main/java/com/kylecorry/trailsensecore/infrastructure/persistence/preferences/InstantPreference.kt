package com.kylecorry.trailsensecore.infrastructure.persistence.preferences

import com.kylecorry.trailsensecore.infrastructure.persistence.Cache
import java.time.Instant
import kotlin.reflect.KProperty

class InstantPreference(
    private val cache: Cache,
    private val name: String,
    private val defaultValue: Instant
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Instant {
        return cache.getInstant(name) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Instant) {
        cache.putInstant(name, value)
    }

}