package com.kylecorry.trailsensecore.domain.geo.specifications

import com.kylecorry.andromeda.core.specifications.Specification
import com.kylecorry.trailsensecore.domain.geo.ApproximateCoordinate

class ApproximatelySameLocationSpecification(private val firstLocation: ApproximateCoordinate): Specification<ApproximateCoordinate>() {
    override fun isSatisfiedBy(value: ApproximateCoordinate): Boolean {
        val distance = value.coordinate.distanceTo(firstLocation.coordinate)
        val maxDistance = value.accuracy.meters().distance * 2 + firstLocation.accuracy.meters().distance * 2
        return distance <= maxDistance
    }
}