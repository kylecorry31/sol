package com.kylecorry.sol.math.algebra

import org.junit.jupiter.api.Assertions.assertEquals
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
}
