package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector3Precise
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RotationTest {

    private val expected = Rotation.fromAxisAngle(Vector3Precise(0.3, -0.5, 0.8), 37.0)
    private val vectors = listOf(
        Vector3Precise(1.0, 0.0, 0.0),
        Vector3Precise(0.0, 1.0, 0.0),
        Vector3Precise(0.0, 0.0, 1.0),
        Vector3Precise(1.0, 1.0, 1.0).normalize()
    )

    @Test
    fun fromAxisAngle() {
        val rotation = Rotation.fromAxisAngle(Vector3Precise(0.0, 0.0, 1.0), 90.0)
        assertVector(Vector3Precise(0.0, 1.0, 0.0), rotation.rotate(Vector3Precise(1.0, 0.0, 0.0)))
        assertEquals(90.0, rotation.angle(), 1e-9)
    }

    @Test
    fun fit() {
        val rotation = Rotation.fit(vectors, vectors.map { expected.rotate(it) })
        assertRotation(expected, rotation)
    }

    @Test
    fun triad() {
        val rotation = Rotation.triad(vectors[0], vectors[3], expected.rotate(vectors[0]), expected.rotate(vectors[3]))
        assertNotNull(rotation)
        assertRotation(expected, rotation!!)
        assertNull(Rotation.triad(vectors[0], vectors[0], vectors[1], vectors[2]))
    }

    @Test
    fun inverse() {
        assertRotation(Rotation.identity, expected * expected.inverse())
    }

    private fun assertRotation(expected: Rotation, actual: Rotation) {
        val e = expected.toDoubleArray()
        val a = actual.toDoubleArray()
        for (i in e.indices) {
            assertEquals(e[i], a[i], 1e-9)
        }
    }

    private fun assertVector(expected: Vector3Precise, actual: Vector3Precise) {
        assertEquals(expected.x, actual.x, 1e-9)
        assertEquals(expected.y, actual.y, 1e-9)
        assertEquals(expected.z, actual.z, 1e-9)
    }
}
