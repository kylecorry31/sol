package com.kylecorry.trailsensecore.domain.geo.specifications

import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.trailsensecore.domain.geo.ApproximateCoordinate
import com.kylecorry.trailsensecore.domain.specifications.Specification

class LocationChangedSpecification(private val previousLocation: ApproximateCoordinate, private val minDistance: Distance): Specification<ApproximateCoordinate>() {
    override fun isSatisfiedBy(value: ApproximateCoordinate): Boolean {
        val isFarEnoughAway = InGeofenceSpecification(previousLocation.coordinate, minDistance).not()
        val areDifferentLocations = ApproximatelySameLocationSpecification(previousLocation).not()
        return isFarEnoughAway.isSatisfiedBy(value.coordinate) && areDifferentLocations.isSatisfiedBy(value)
    }
}