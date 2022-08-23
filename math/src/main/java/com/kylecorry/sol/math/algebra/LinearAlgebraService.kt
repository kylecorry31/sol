package com.kylecorry.sol.math.algebra

import kotlin.math.max
import kotlin.math.min

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
        if (mat1.columns() != mat2.columns() && mat2.columns() != 1) {
            throw Exception("Matrix 1 columns must be the same size as matrix 2 columns")
        }

        if (mat1.rows() != mat2.rows() && mat2.rows() != 1) {
            throw Exception("Matrix 1 rows must be the same size as matrix 2 rows")
        }

        return createMatrix(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] - mat2[min(row, mat2.rows() - 1), min(col, mat2.columns() - 1)]
        }
    }

    fun add(mat1: Matrix, mat2: Matrix): Matrix {
        if (mat1.columns() != mat2.columns() && mat2.columns() != 1) {
            throw Exception("Matrix 1 columns must be the same size as matrix 2 columns")
        }

        if (mat1.rows() != mat2.rows() && mat2.rows() != 1) {
            throw Exception("Matrix 1 rows must be the same size as matrix 2 rows")
        }

        return createMatrix(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] + mat2[min(row, mat2.rows() - 1), min(col, mat2.columns() - 1)]
        }
    }

    fun multiply(mat1: Matrix, mat2: Matrix): Matrix {
        if (mat1.columns() != mat2.columns() && mat2.columns() != 1) {
            throw Exception("Matrix 1 columns must be the same size as matrix 2 columns")
        }

        if (mat1.rows() != mat2.rows() && mat2.rows() != 1) {
            throw Exception("Matrix 1 rows must be the same size as matrix 2 rows")
        }

        return createMatrix(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] * mat2[min(row, mat2.rows() - 1), min(col, mat2.columns() - 1)]
        }
    }

    fun multiply(mat1: Matrix, scale: Float): Matrix {
        return createMatrix(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] * scale
        }
    }

    fun divide(mat1: Matrix, mat2: Matrix): Matrix {
        if (mat1.columns() != mat2.columns() && mat2.columns() != 1) {
            throw Exception("Matrix 1 columns must be the same size as matrix 2 columns")
        }

        if (mat1.rows() != mat2.rows() && mat2.rows() != 1) {
            throw Exception("Matrix 1 rows must be the same size as matrix 2 rows")
        }

        return createMatrix(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] / mat2[min(row, mat2.rows() - 1), min(col, mat2.columns() - 1)]
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

    fun mapRows(mat: Matrix, fn: (row: FloatArray) -> FloatArray): Matrix {
        return mat.map { fn(it.toFloatArray()).toTypedArray() }.toTypedArray()
    }

    fun mapColumns(mat: Matrix, fn: (row: FloatArray) -> FloatArray): Matrix {
        return mapRows(mat.transpose(), fn).transpose()
    }

    fun sum(mat: Matrix): Float {
        return mat.sumOf { it.sum().toDouble() }.toFloat()
    }

    fun sumColumns(mat: Matrix): Matrix {
        return sumRows(mat.transpose()).transpose()
    }

    fun sumRows(mat: Matrix): Matrix {
        return createMatrix(mat.rows(), 1) { row, _ ->
            mat[row].sum()
        }
    }

    fun max(mat: Matrix): Float {
        return mat.maxOf { it.max() }
    }

    fun maxColumns(mat: Matrix): Matrix {
        return maxRows(mat.transpose()).transpose()
    }

    fun maxRows(mat: Matrix): Matrix {
        return createMatrix(mat.rows(), 1) { row, _ ->
            mat[row].max()
        }
    }

}