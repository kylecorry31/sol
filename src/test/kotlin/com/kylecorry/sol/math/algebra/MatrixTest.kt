package com.kylecorry.sol.math.algebra

import assertk.assertFailure
import com.kylecorry.sol.math.Vector
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class MatrixTest {

    @ParameterizedTest
    @CsvSource(
        "2, 3, 6",
        "0, 3, 0",
        "3, 0, 0",
        "0, 0, 0",
        "1, 3, 3",
        "3, 1, 3",
        "3, 3, 9"
    )
    fun size(rows: Int, columns: Int, expected: Int) {
        val matrix = Matrix.create(rows, columns, 0f)
        assertEquals(expected, matrix.size())
    }

    @Test
    fun toVectorFromRowMatrix() {
        val matrix = Matrix.row(1f, 2f, 3f)

        val actual = matrix.toVector()

        assertVectorEquals(Vector.from(1f, 2f, 3f), actual)
    }

    @Test
    fun toVectorFromColumnMatrix() {
        val matrix = Matrix.column(1f, 2f, 3f)

        val actual = matrix.toVector()

        assertVectorEquals(Vector.from(1f, 2f, 3f), actual)
    }

    @Test
    fun toVectorRequiresRowOrColumnMatrix() {
        val matrix = Matrix.create(
            arrayOf(
                floatArrayOf(1f, 2f),
                floatArrayOf(3f, 4f)
            )
        )

        assertFailure {
            matrix.toVector()
        }
    }

    private fun assertVectorEquals(expected: Vector, actual: Vector) {
        assertEquals(expected.size, actual.size)
        for (i in 0..<expected.size) {
            assertEquals(expected[i], actual[i])
        }
    }
}
