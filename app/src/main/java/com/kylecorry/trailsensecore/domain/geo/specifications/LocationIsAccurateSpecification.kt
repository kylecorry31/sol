package com.kylecorry.trailsensecore.domain.geo.specifications

import com.kylecorry.trailsensecore.domain.geo.ApproximateCoordinate
import com.kylecorry.andromeda.core.specifications.Specification

class LocationIsAccurateSpecification: Specification<ApproximateCoordinate>() {
    override fun isSatisfiedBy(value: ApproximateCoordinate): Boolean {
        return value.accuracy.meters().distance <= 16f
    }
}