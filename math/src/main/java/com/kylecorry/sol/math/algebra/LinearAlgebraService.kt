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

    fun subtract(mat1: Matrix, mat2: Matrix): Matrix {
        if (mat1.columns() != mat2.columns()) {
            throw Exception("Matrix 1 columns must be the same size as matrix 2 columns")
        }

        if (mat1.rows() != mat2.rows()) {
            throw Exception("Matrix 1 rows must be the same size as matrix 2 rows")
        }

        return createMatrix(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] - mat2[row, col]
        }
    }

    fun add(mat1: Matrix, mat2: Matrix): Matrix {
        if (mat1.columns() != mat2.columns()) {
            throw Exception("Matrix 1 columns must be the same size as matrix 2 columns")
        }

        if (mat1.rows() != mat2.rows()) {
            throw Exception("Matrix 1 rows must be the same size as matrix 2 rows")
        }

        return createMatrix(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] + mat2[row, col]
        }
    }

    fun multiply(mat1: Matrix, mat2: Matrix): Matrix {
        if (mat1.columns() != mat2.columns()) {
            throw Exception("Matrix 1 columns must be the same size as matrix 2 columns")
        }

        if (mat1.rows() != mat2.rows()) {
            throw Exception("Matrix 1 rows must be the same size as matrix 2 rows")
        }

        return createMatrix(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] * mat2[row, col]
        }
    }

    fun multiply(mat1: Matrix, scale: Float): Matrix {
        return createMatrix(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] * scale
        }
    }

    fun divide(mat1: Matrix, mat2: Matrix): Matrix {
        if (mat1.columns() != mat2.columns()) {
            throw Exception("Matrix 1 columns must be the same size as matrix 2 columns")
        }

        if (mat1.rows() != mat2.rows()) {
            throw Exception("Matrix 1 rows must be the same size as matrix 2 rows")
        }

        return createMatrix(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] / mat2[row, col]
        }
    }

    fun divide(mat1: Matrix, scale: Float): Matrix {
        return createMatrix(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] / scale
        }
    }

    fun transpose(mat: Matrix): Matrix {
        return createMatrix(mat.columns(), mat.rows()) { row, col ->
            mat[col, row]
        }
    }

    fun map(mat: Matrix, fn: (value: Float) -> Float): Matrix {
        return createMatrix(mat.rows(), mat.columns()) { row, col ->
            fn(mat[row, col])
        }
    }

    fun sum(mat: Matrix): Float {
        return mat.sumOf { it.sum().toDouble() }.toFloat()
    }

}