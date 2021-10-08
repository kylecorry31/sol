package com.kylecorry.sol.math.algebra

class LinearAlgebraService {

    fun dot(mat1: Matrix, mat2: Matrix): Matrix {
        if (mat1.columns() != mat2.rows()) {
            throw Exception("Matrix 1 columns must be the same size as matrix 2 rows")
        }

        val product = createMatrix(mat1.rows(), mat2.columns()) { _, _ -> 0f }
        for (r in 0 until mat1.rows()) {
            for (otherC in 0 until mat2.columns()) {
                var sum = 0.0f
                for (c in 0 until mat1.columns()) {
                    sum += mat1[r, c] * mat2[c, otherC]
                }
                product[r, otherC] = sum
            }
        }

        return product
    }

}