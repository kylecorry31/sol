package com.kylecorry.sol.science.geology.specifications

import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.CompassDirection
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.science.geology.ApproximateCoordinate
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