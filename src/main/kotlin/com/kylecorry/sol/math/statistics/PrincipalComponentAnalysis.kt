package com.kylecorry.sol.math.statistics

import com.kylecorry.sol.math.algebra.Matrix
import com.kylecorry.sol.math.algebra.LinearAlgebra
import com.kylecorry.sol.math.algebra.dot
import com.kylecorry.sol.math.algebra.subtract
import com.kylecorry.sol.math.algebra.transpose

internal object PrincipalComponentAnalysis {

    fun getComponents(x: Matrix, k: Int): Matrix {
        require(k >= 0) { "k must be non-negative" }
        require(k <= x.columns()) { "k must be less than or equal to the number of features" }

        if (k == 0 || x.rows() == 0) {
            return Matrix.zeros(x.rows(), k)
        }

        val featureMean = Matrix.create(1, x.columns()) { _, column ->
            x.getColumn(column).average().toFloat()
        }
        val xTilde = x.subtract(featureMean)
        val x2 = xTilde.transpose().dot(xTilde)
        val eigen = LinearAlgebra.eigen(x2, TOLERANCE, MAX_ITERATIONS)
        val sortedIndices = (0..<eigen.values.size).sortedByDescending { eigen.values[it] }
        val components = Matrix.create(eigen.vectors.rows(), k) { row, column ->
            eigen.vectors[row, sortedIndices[column]]
        }
        return xTilde.dot(components)
    }

    private const val TOLERANCE = 1e-6f
    private const val MAX_ITERATIONS = 100

}
