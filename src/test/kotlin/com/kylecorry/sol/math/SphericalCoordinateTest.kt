package com.kylecorry.sol.math

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SphericalCoordinateTest {

    @ParameterizedTest
    @CsvSource(
        "1, 45, 45, 0.5, 0.5, 0.707",
        "1, 0, 0, 0, 0, 1",
        "1, 90, 0, 1, 0, 0",
        "1, 0, 90, 0, 0, 1",
        "1, 180, 0, 0, 0, -1",
        "1, 0, 180, 0, 0, 1",
        "1, 270, 0, -1, 0, 0",
        "1, 0, 270, 0, 0, 1",
        "15, 20, 10, 5.05, 0.89, 14.1",
        "0, 20, 10, 0, 0, 0"
    )
    fun toCartesian(r: Float, theta: Float, phi: Float, x: Float, y: Float, z: Float) {
        val spherical = SphericalCoordinate(r, theta, phi)
        val cartesian = spherical.toCartesian()
        approxEquals(Vector3(x, y, z), cartesian, 0.01f)
    }

    @ParameterizedTest
    @CsvSource(
        "0.5, 0.5, 0.707, 1, 45, 45",
        "0, 0, 1, 1, 0, 0",
        "1, 0, 0, 1, 90, 0",
        "0, 0, 1, 1, 0, 0",
        "0, 0, -1, 1, 180, 0",
//        "-1, 0, 0, 1, 0, 0",
        "5.05, 0.89, 14.1, 15, 20, 10",
        "0, 0, 0, 0, 0, 0"
    )
    fun fromCartesian(x: Float, y: Float, z: Float, r: Float, theta: Float, phi: Float) {
        val cartesian = Vector3(x, y, z)
        val spherical = SphericalCoordinate.fromCartesian(cartesian)
        approxEquals(SphericalCoordinate(r, theta, phi), spherical, 0.1f)
    }

    private fun approxEquals(expected: Vector3, actual: Vector3, tolerance: Float) {
        assertEquals(expected.x, actual.x, tolerance)
        assertEquals(expected.y, actual.y, tolerance)
        assertEquals(expected.z, actual.z, tolerance)
    }

    private fun approxEquals(expected: SphericalCoordinate, actual: SphericalCoordinate, tolerance: Float) {
        assertEquals(expected.r, actual.r, tolerance)
        assertEquals(expected.theta, actual.theta, tolerance)
        assertEquals(expected.phi, actual.phi, tolerance)
    }
}