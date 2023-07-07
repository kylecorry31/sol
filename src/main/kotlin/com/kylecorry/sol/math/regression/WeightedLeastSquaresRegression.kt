package com.kylecorry.sol.math.regression

import com.kylecorry.sol.math.algebra.*

class WeightedLeastSquaresRegression(
    private val input: List<List<Float>>,
    private val output: List<Float>,
    private val weights: List<Float>
) : IRegression {

    private val inputs = input.firstOrNull()?.size ?: 0
    val b = fit()
    val coefs = b.transpose()[0].toList()

    override fun predict(x: List<Float>): Float {
        val input = rowMatrix(values = (x + listOf(1f)).toFloatArray())
        return input.dot(b)[0, 0]
    }

    private fun fit(): Matrix {
        if (input.size <= 1) {
            return columnMatrix(values = FloatArray(inputs + 1))
        }

        val w = diagonalMatrix(values = weights.toFloatArray())
        val x = createMatrix(input.size, inputs) { r, c ->
            input[r][c]
        }.appendColumn(1f)
        val y = columnMatrix(values = output.toFloatArray())
        val xtw = x.transpose().dot(w)
        return xtw.dot(x).inverse().dot(xtw.dot(y))
    }

}