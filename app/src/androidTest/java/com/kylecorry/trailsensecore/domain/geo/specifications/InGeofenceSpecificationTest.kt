package com.kylecorry.trailsensecore.domain.geo.specifications

import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.trailsensecore.geology.specifications.InGeofenceSpecification
import org.junit.Assert.assertEquals
import org.junit.Test

class InGeofenceSpecificationTest {
    @Test
    fun satisfiedWhenLocationIsTheCenter() {
        val center = Coordinate(0.0, 0.0)
        val radius = Distance.meters(10f)
        val point = Coordinate(0.0, 0.0)
        val isSatisfied = true

        val spec = InGeofenceSpecification(center, radius)
        assertEquals(isSatisfied, spec.isSatisfiedBy(point))
    }

    @Test
    fun satisfiedWhenLocationInInGeofence() {
        val center = Coordinate(0.0, 0.0)
        val radius = Distance.meters(10f)
        val point = Coordinate(0.00001, 0.00001)
        val isSatisfied = true

        val spec = InGeofenceSpecification(center, radius)
        assertEquals(isSatisfied, spec.isSatisfiedBy(point))
    }

    @Test
    fun notSatisfiedWhenLocationOutsideGeoFense() {
        val center = Coordinate(0.0, 0.0)
        val radius = Distance.meters(10f)
        val point = Coordinate(1.0, 1.0)
        val isSatisfied = false

        val spec = InGeofenceSpecification(center, radius)
        assertEquals(isSatisfied, spec.isSatisfiedBy(point))
    }
}