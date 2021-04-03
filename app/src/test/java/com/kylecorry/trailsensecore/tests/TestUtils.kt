package com.kylecorry.trailsensecore.tests

import org.junit.Assert
import java.time.Duration
import java.time.ZonedDateTime

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
        Assert.assertNull(actual)
    } else {
        Assert.assertNotNull("Expected $expected, but was null", actual)
        val diff = Duration.between(expected, actual)
        Assert.assertTrue("Expected $expected, found $actual", diff.abs() <= maxDifference)
    }
}
fun assertDuration(
    expected: Duration?,
    actual: Duration?,
    maxDifference: Duration
){
    if (expected == null) {
        Assert.assertNull(actual)
    } else {
        Assert.assertNotNull("Expected $expected, but was null", actual)
        val diff = expected.minus(actual)
        Assert.assertTrue("Expected $expected, found $actual", diff.abs() <= maxDifference)
    }
}