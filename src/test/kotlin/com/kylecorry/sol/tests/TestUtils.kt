package com.kylecorry.sol.tests

import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime
import assertk.assertions.isLessThan
import assertk.assertions.prop
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import org.junit.jupiter.api.Assertions.*

fun assertk.Assert<Coordinate>.isCloseTo(other: Coordinate, meters: Float) {
    prop("distance") {
        it.distanceTo(other)
    }.isLessThan(meters)
}

fun assertk.Assert<Distance>.isCloseTo(other: Distance, tolerance: Float) {
    prop("units", Distance::units).isEqualTo(other.units)
    prop("value", Distance::distance).isCloseTo(other.distance, tolerance)
}

fun <T> parametrized(cases: Collection<T>, test: (case: T) -> Unit) {
    for (case in cases) {
        test(case)
    }
}

fun assertDate(
    expected: ZonedDateTime?,
    actual: ZonedDateTime?,
    maxDifference: Duration
) {
    if (expected == null) {
        assertNull(actual)
    } else {
        assertNotNull(actual, "Expected $expected, but was null")
        val diff = Duration.between(expected, actual)
        assertTrue(diff.abs() <= maxDifference, "Expected $expected, found $actual")
    }
}

fun assertDate(
    expected: LocalDateTime?,
    actual: LocalDateTime?,
    maxDifference: Duration
) {
    if (expected == null) {
        assertNull(actual)
    } else {
        assertNotNull(actual, "Expected $expected, but was null")
        val diff = Duration.between(expected, actual)
        assertTrue(diff.abs() <= maxDifference, "Expected $expected, found $actual")
    }
}

fun assertDuration(
    expected: Duration?,
    actual: Duration?,
    maxDifference: Duration
){
    if (expected == null) {
        assertNull(actual)
    } else {
        assertNotNull(actual, "Expected $expected, but was null")
        val diff = expected.minus(actual)
        assertTrue(diff.abs() <= maxDifference, "Expected $expected, found $actual")
    }
}