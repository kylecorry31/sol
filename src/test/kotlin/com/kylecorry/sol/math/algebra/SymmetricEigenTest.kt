package com.kylecorry.sol.math.algebra

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.abs

class SymmetricEigenTest {

    @Test
    fun decompose() {
        val matrix = arrayOf(
            doubleArrayOf(4.0, 1.0, 2.0),
            doubleArrayOf(1.0, 3.0, 0.5),
            doubleArrayOf(2.0, 0.5, 5.0)
        )
        val (values, vectors) = SymmetricEigen.decompose(matrix)
        for (i in values.indices) {
            val v = vectors[i]
            for (row in 0..2) {
                val product = (0..2).sumOf { matrix[row][it] * v[it] }
                assertEquals(values[i] * v[row], product, 1e-9)
            }
        }
        assertEquals(12.0, values.sum(), 1e-9)
    }

    @Test
    fun largestEigenvector() {
        val matrix = arrayOf(
            doubleArrayOf(2.0, 0.0),
            doubleArrayOf(0.0, 7.0)
        )
        val vector = SymmetricEigen.largestEigenvector(matrix)
        assertEquals(0.0, vector[0], 1e-12)
        assertEquals(1.0, abs(vector[1]), 1e-12)
    }
}
