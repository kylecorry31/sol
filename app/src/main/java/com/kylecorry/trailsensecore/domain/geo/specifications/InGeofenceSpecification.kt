package com.kylecorry.trailsensecore.domain.geo.specifications

import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.domain.specifications.Specification
import com.kylecorry.trailsensecore.domain.units.Distance

class InGeofenceSpecification(private val center: Coordinate, private val radius: Distance) :
    Specification<Coordinate>() {
    override fun isSatisfiedBy(value: Coordinate): Boolean {
        val distance = center.distanceTo(value)
        return distance <= radius.meters().distance
    }
}