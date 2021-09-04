package com.kylecorry.trailsensecore.domain.units

import com.kylecorry.andromeda.core.units.DistanceUnits
import com.kylecorry.andromeda.core.specifications.Specification

class IsLargeUnitSpecification: Specification<DistanceUnits>() {
    override fun isSatisfiedBy(value: DistanceUnits): Boolean {
        val largeUnits = listOf(DistanceUnits.Miles, DistanceUnits.Kilometers, DistanceUnits.NauticalMiles)
        return largeUnits.contains(value)
    }
}