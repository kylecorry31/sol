package com.kylecorry.trailsensecore.domain.geo.specifications

import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.trailsensecore.domain.specifications.Specification

class InGeofenceSpecification(private val center: Coordinate, private val radius: Distance) :
    Specification<Coordinate>() {
    override fun isSatisfiedBy(value: Coordinate): Boolean {
        val distance = center.distanceTo(value)
        return distance <= radius.meters().distance
    }
}