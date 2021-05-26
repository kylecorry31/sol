package com.kylecorry.trailsensecore.infrastructure.persistence.preferences

import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.infrastructure.persistence.Cache
import kotlin.reflect.KProperty

class CoordinatePreference(
    private val cache: Cache,
    private val name: String,
    private val defaultValue: Coordinate
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Coordinate {
        return cache.getCoordinate(name) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Coordinate) {
        cache.putCoordinate(name, value)
    }

}