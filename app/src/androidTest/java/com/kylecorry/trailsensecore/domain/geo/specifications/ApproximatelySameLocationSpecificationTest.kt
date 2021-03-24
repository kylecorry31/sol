package com.kylecorry.trailsensecore.domain.geo.specifications

import com.kylecorry.trailsensecore.domain.geo.ApproximateCoordinate
import com.kylecorry.trailsensecore.domain.geo.Bearing
import com.kylecorry.trailsensecore.domain.geo.CompassDirection
import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.domain.units.Distance
import org.junit.Assert
import org.junit.Test

class ApproximatelySameLocationSpecificationTest {

    @Test
    fun satisfiedWhenLocationIsTheSame() {
        val start = ApproximateCoordinate(0.0, 0.0, Distance.meters(10f))
        val end = ApproximateCoordinate(0.0, 0.0, Distance.meters(10f))
        val isSatisfied = true

        val spec = ApproximatelySameLocationSpecification(start)
        Assert.assertEquals(isSatisfied, spec.isSatisfiedBy(end))
    }

    @Test
    fun satisfiedWhenLocationsAreClose() {
        val start = ApproximateCoordinate(0.0, 0.0, Distance.meters(10f))
        val end = ApproximateCoordinate.from(
            Coordinate.zero.plus(
                60.0,
                Bearing.from(CompassDirection.North)
            ), Distance.meters(20f)
        )
        val isSatisfied = true

        val spec = ApproximatelySameLocationSpecification(start)
        Assert.assertEquals(isSatisfied, spec.isSatisfiedBy(end))
    }

    @Test
    fun notSatisfiedWhenLocationsAreFar() {
        val start = ApproximateCoordinate(0.0, 0.0, Distance.meters(10f))
        val end = ApproximateCoordinate.from(
            Coordinate.zero.plus(
                61.0,
                Bearing.from(CompassDirection.North)
            ), Distance.meters(20f)
        )
        val isSatisfied = false

        val spec = ApproximatelySameLocationSpecification(start)
        Assert.assertEquals(isSatisfied, spec.isSatisfiedBy(end))
    }
}