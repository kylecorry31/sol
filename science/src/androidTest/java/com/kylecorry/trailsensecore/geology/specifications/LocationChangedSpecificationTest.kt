package com.kylecorry.trailsensecore.geology.specifications

import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.trailsensecore.geology.ApproximateCoordinate
import com.kylecorry.trailsensecore.geology.specifications.LocationChangedSpecification
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationChangedSpecificationTest {

    @Test
    fun unsatisfiedWhenLocationIsTheSame() {
        val start = ApproximateCoordinate(0.0, 0.0, Distance.meters(10f))
        val end = ApproximateCoordinate(0.0, 0.0, Distance.meters(10f))
        val isSatisfied = false

        val spec = LocationChangedSpecification(start, Distance.meters(10f))
        assertEquals(isSatisfied, spec.isSatisfiedBy(end))
    }

    @Test
    fun unsatisfiedWhenLocationsAreDifferentWithDistanceButNotAccuracy() {
        val start = ApproximateCoordinate(0.0, 0.0, Distance.meters(100f))
        val end = ApproximateCoordinate(0.0001, 0.0001, Distance.meters(10f))
        val isSatisfied = false

        val spec = LocationChangedSpecification(start, Distance.meters(1f))
        assertEquals(isSatisfied, spec.isSatisfiedBy(end))
    }

    @Test
    fun unsatisfiedWhenLocationsAreDifferentWithAccuracyButNotDistance() {
        val start = ApproximateCoordinate(0.0, 0.0, Distance.meters(1f))
        val end = ApproximateCoordinate(0.00001, 0.00001, Distance.meters(1f))
        val isSatisfied = false

        val spec = LocationChangedSpecification(start, Distance.meters(10f))
        assertEquals(isSatisfied, spec.isSatisfiedBy(end))
    }

    @Test
    fun satisfiedWhenLocationsAreDifferent() {
        val start = ApproximateCoordinate(0.0, 0.0, Distance.meters(10f))
        val end = ApproximateCoordinate(0.1, 0.1, Distance.meters(10f))
        val isSatisfied = true

        val spec = LocationChangedSpecification(start, Distance.meters(10f))
        assertEquals(isSatisfied, spec.isSatisfiedBy(end))
    }

}