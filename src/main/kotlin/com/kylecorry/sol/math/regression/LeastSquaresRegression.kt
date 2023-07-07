package com.kylecorry.sol.math.regression

import com.kylecorry.sol.math.algebra.*

class LeastSquaresRegression(
    private val input: List<List<Float>>,
    private val output: List<Float>
): IRegression {

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

        val x = createMatrix(input.size, inputs) { r, c ->
            input[r][c]
        }.appendColumn(1f)
        val y = columnMatrix(values = output.toFloatArray())
        val xt = x.transpose()
        return xt.dot(x).inverse().dot(xt.dot(y))
    }

}