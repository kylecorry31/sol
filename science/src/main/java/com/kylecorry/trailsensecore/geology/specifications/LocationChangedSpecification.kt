package com.kylecorry.trailsensecore.geology.specifications

import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.trailsensecore.geology.ApproximateCoordinate
import com.kylecorry.andromeda.core.specifications.Specification

class LocationChangedSpecification(private val previousLocation: ApproximateCoordinate, private val minDistance: Distance): Specification<ApproximateCoordinate>() {
    override fun isSatisfiedBy(value: ApproximateCoordinate): Boolean {
        val isFarEnoughAway = InGeofenceSpecification(previousLocation.coordinate, minDistance).not()
        val areDifferentLocations = ApproximatelySameLocationSpecification(previousLocation).not()
        return isFarEnoughAway.isSatisfiedBy(value.coordinate) && areDifferentLocations.isSatisfiedBy(value)
    }
}