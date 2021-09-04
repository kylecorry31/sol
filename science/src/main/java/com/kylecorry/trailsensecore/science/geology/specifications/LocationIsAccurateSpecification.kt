package com.kylecorry.trailsensecore.science.geology.specifications

import com.kylecorry.trailsensecore.science.geology.ApproximateCoordinate
import com.kylecorry.andromeda.core.specifications.Specification

class LocationIsAccurateSpecification: Specification<ApproximateCoordinate>() {
    override fun isSatisfiedBy(value: ApproximateCoordinate): Boolean {
        return value.accuracy.meters().distance <= 16f
    }
}