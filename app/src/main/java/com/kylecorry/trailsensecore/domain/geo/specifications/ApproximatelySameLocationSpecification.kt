package com.kylecorry.trailsensecore.domain.geo.specifications

import com.kylecorry.trailsensecore.domain.geo.ApproximateCoordinate
import com.kylecorry.trailsensecore.domain.specifications.Specification

class ApproximatelySameLocationSpecification(private val firstLocation: ApproximateCoordinate): Specification<ApproximateCoordinate>() {
    override fun isSatisfiedBy(value: ApproximateCoordinate): Boolean {
        val distance = value.coordinate.distanceTo(firstLocation.coordinate)
        val maxDistance = value.accuracy.meters().distance * 2 + firstLocation.accuracy.meters().distance * 2
        return distance <= maxDistance
    }
}