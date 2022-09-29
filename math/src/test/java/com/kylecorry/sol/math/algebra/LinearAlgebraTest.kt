package com.kylecorry.sol.math.algebra

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LinearAlgebraTest {

    @Test
    fun dot() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val m2 = createMatrix(2, 3, 0f)
        m2[0, 0] = 1f
        m2[0, 1] = 2f
        m2[0, 2] = 3f
        m2[1, 0] = 4f
        m2[1, 1] = 5f
        m2[1, 2] = 6f

        val expected = createMatrix(3, 3, 0f)
        expected[0, 0] = 9f
        expected[0, 1] = 12f
        expected[0, 2] = 15f
        expected[1, 0] = 19f
        expected[1, 1] = 26f
        expected[1, 2] = 33f
        expected[2, 0] = 29f
        expected[2, 1] = 40f
        expected[2, 2] = 51f

        val result = LinearAlgebra.dot(m1, m2)

        assertEquals(expected, result)
    }

    @Test
    fun subtract() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val m2 = createMatrix(3, 2, 0f)
        m2[0, 0] = 2f
        m2[0, 1] = 1f
        m2[1, 0] = 5f
        m2[1, 1] = 4f
        m2[2, 0] = 1f
        m2[2, 1] = 5f

        val expected = createMatrix(3, 2, 0f)
        expected[0, 0] = -1f
        expected[0, 1] = 1f
        expected[1, 0] = -2f
        expected[1, 1] = 0f
        expected[2, 0] = 4f
        expected[2, 1] = 1f

        val result = LinearAlgebra.subtract(m1, m2)

        assertEquals(expected, result)
    }

    @Test
    fun subtractScalar() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val expected = createMatrix(3, 2, 0f)
        expected[0, 0] = -1f
        expected[0, 1] = 0f
        expected[1, 0] = 1f
        expected[1, 1] = 2f
        expected[2, 0] = 3f
        expected[2, 1] = 4f

        val result = LinearAlgebra.subtract(m1, 2f)

        assertEquals(expected, result)
    }

    // TODO: Subtract column / row vector

    @Test
    fun add() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val m2 = createMatrix(3, 2, 0f)
        m2[0, 0] = 2f
        m2[0, 1] = 1f
        m2[1, 0] = 5f
        m2[1, 1] = 4f
        m2[2, 0] = 1f
        m2[2, 1] = 5f

        val expected = createMatrix(3, 2, 0f)
        expected[0, 0] = 3f
        expected[0, 1] = 3f
        expected[1, 0] = 8f
        expected[1, 1] = 8f
        expected[2, 0] = 6f
        expected[2, 1] = 11f

        val result = LinearAlgebra.add(m1, m2)

        assertEquals(expected, result)
    }

    @Test
    fun addScalar() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val expected = createMatrix(3, 2, 0f)
        expected[0, 0] = 3f
        expected[0, 1] = 4f
        expected[1, 0] = 5f
        expected[1, 1] = 6f
        expected[2, 0] = 7f
        expected[2, 1] = 8f

        val result = LinearAlgebra.add(m1, 2f)

        assertEquals(expected, result)
    }

    // TODO: Add column / row vector

    @Test
    fun multiply() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val m2 = createMatrix(3, 2, 0f)
        m2[0, 0] = 2f
        m2[0, 1] = 1f
        m2[1, 0] = 5f
        m2[1, 1] = 4f
        m2[2, 0] = 1f
        m2[2, 1] = 5f

        val expected = createMatrix(3, 2, 0f)
        expected[0, 0] = 2f
        expected[0, 1] = 2f
        expected[1, 0] = 15f
        expected[1, 1] = 16f
        expected[2, 0] = 5f
        expected[2, 1] = 30f

        val result = LinearAlgebra.multiply(m1, m2)

        assertEquals(expected, result)
    }

    @Test
    fun multiplyScalar() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val expected = createMatrix(3, 2, 0f)
        expected[0, 0] = 2f
        expected[0, 1] = 4f
        expected[1, 0] = 6f
        expected[1, 1] = 8f
        expected[2, 0] = 10f
        expected[2, 1] = 12f

        val result = LinearAlgebra.multiply(m1, 2f)

        assertEquals(expected, result)
    }

    // TODO: Multiply column / row vector

    @Test
    fun divide() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val m2 = createMatrix(3, 2, 0f)
        m2[0, 0] = 2f
        m2[0, 1] = 1f
        m2[1, 0] = 5f
        m2[1, 1] = 4f
        m2[2, 0] = 1f
        m2[2, 1] = 5f

        val expected = createMatrix(3, 2, 0f)
        expected[0, 0] = 0.5f
        expected[0, 1] = 2f
        expected[1, 0] = 0.6f
        expected[1, 1] = 1f
        expected[2, 0] = 5f
        expected[2, 1] = 1.2f

        val result = LinearAlgebra.divide(m1, m2)

        assertEquals(expected, result)
    }

    @Test
    fun divideScalar() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val expected = createMatrix(3, 2, 0f)
        expected[0, 0] = 0.5f
        expected[0, 1] = 1f
        expected[1, 0] = 1.5f
        expected[1, 1] = 2f
        expected[2, 0] = 2.5f
        expected[2, 1] = 3f

        val result = LinearAlgebra.divide(m1, 2f)

        assertEquals(expected, result)
    }

    // TODO: Divide column / row vector

    @Test
    fun transpose() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val expected = createMatrix(2, 3, 0f)
        expected[0, 0] = 1f
        expected[0, 1] = 3f
        expected[0, 2] = 5f
        expected[1, 0] = 2f
        expected[1, 1] = 4f
        expected[1, 2] = 6f

        val result = LinearAlgebra.transpose(m1)

        assertEquals(expected, result)
    }

    @Test
    fun map() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val expected = createMatrix(3, 2, 0f)
        expected[0, 0] = 2f
        expected[0, 1] = 4f
        expected[1, 0] = 6f
        expected[1, 1] = 8f
        expected[2, 0] = 10f
        expected[2, 1] = 12f

        val result = LinearAlgebra.map(m1){ it * 2 }

        assertEquals(expected, result)
    }

    // TODO: Map rows and columns

    @Test
    fun sum() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val result = LinearAlgebra.sum(m1)

        assertEquals(21f, result)
    }

    @Test
    fun sumRows() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val expected = createMatrix(3, 1, 0f)
        expected[0, 0] = 3f
        expected[1, 0] = 7f
        expected[2, 0] = 11f

        val result = LinearAlgebra.sumRows(m1)

        assertEquals(expected, result)
    }

    @Test
    fun sumColumns() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val expected = createMatrix(1, 2, 0f)
        expected[0, 0] = 9f
        expected[0, 1] = 12f

        val result = LinearAlgebra.sumColumns(m1)

        assertEquals(expected, result)
    }

    @Test
    fun max() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val result = LinearAlgebra.max(m1)

        assertEquals(6f, result)
    }

    @Test
    fun maxRows() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val expected = createMatrix(3, 1, 0f)
        expected[0, 0] = 2f
        expected[1, 0] = 4f
        expected[2, 0] = 6f

        val result = LinearAlgebra.maxRows(m1)

        assertEquals(expected, result)
    }

    @Test
    fun maxColumns() {
        val m1 = createMatrix(3, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f

        val expected = createMatrix(1, 2, 0f)
        expected[0, 0] = 5f
        expected[0, 1] = 6f

        val result = LinearAlgebra.maxColumns(m1)

        assertEquals(expected, result)
    }

    @Test
    fun inverse2x2() {
        val m1 = createMatrix(2, 2, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[1, 0] = 3f
        m1[1, 1] = 4f

        val expected = createMatrix(2, 2, 0f)
        expected[0, 0] = -2f
        expected[0, 1] = 1f
        expected[1, 0] = 3/2f
        expected[1, 1] = -1/2f

        val actual = LinearAlgebra.inverse(m1)

        assertEquals(expected, actual, 0.00001f)
    }

    @Test
    fun inverse3x3() {
        val m1 = createMatrix(3, 3, 0f)
        m1[0, 0] = 1f
        m1[0, 1] = 2f
        m1[0, 2] = 3f
        m1[1, 0] = 0f
        m1[1, 1] = 1f
        m1[1, 2] = 4f
        m1[2, 0] = 5f
        m1[2, 1] = 6f
        m1[2, 2] = 0f

        val expected = createMatrix(3, 3, 0f)
        expected[0, 0] = -24f
        expected[0, 1] = 18f
        expected[0, 2] = 5f
        expected[1, 0] = 20f
        expected[1, 1] = -15f
        expected[1, 2] = -4f
        expected[2, 0] = -5f
        expected[2, 1] = 4f
        expected[2, 2] = 1f

        val actual = LinearAlgebra.inverse(m1)

        assertEquals(expected, actual, 0.00001f)
    }

    private fun assertEquals(m1: Matrix, m2: Matrix, tolerance: Float = 0f) {
        assertEquals(m1.rows(), m2.rows())
        assertEquals(m1.columns(), m2.columns())

        for (r in 0 until m1.rows()) {
            for (c in 0 until m1.columns()) {
                assertEquals(m1[r, c], m2[r, c], tolerance)
            }
        }
    }


}