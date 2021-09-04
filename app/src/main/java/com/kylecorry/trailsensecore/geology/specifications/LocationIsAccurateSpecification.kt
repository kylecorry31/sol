package com.kylecorry.trailsensecore.geology.specifications

import com.kylecorry.trailsensecore.geology.ApproximateCoordinate
import com.kylecorry.andromeda.core.specifications.Specification

class LocationIsAccurateSpecification: Specification<ApproximateCoordinate>() {
    override fun isSatisfiedBy(value: ApproximateCoordinate): Boolean {
        return value.accuracy.meters().distance <= 16f
    }
}