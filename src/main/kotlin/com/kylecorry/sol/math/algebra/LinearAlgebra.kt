package com.kylecorry.sol.math.algebra

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.shared.ArrayUtils.swap
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.sqrt

object LinearAlgebra {

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

    fun subtract(mat1: Matrix, value: Float): Matrix {
        return add(mat1, -value)
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

    fun add(mat1: Matrix, value: Float): Matrix {
        return createMatrix(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] + value
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
        return multiply(mat1, 1 / scale)
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

    fun inverse(m: Matrix): Matrix {
        if (m.rows() != m.columns()) {
            throw Exception("Matrix must be square to calculate inverse")
        }

        val det = determinant(m)
        if (SolMath.isZero(det)) {
            // No inverse exists
            return createMatrix(m.rows(), m.columns(), 0f)
        }
        return adjugate(m).transpose().divide(determinant(m))
    }

    fun adjugate(m: Matrix): Matrix {
        if (m.rows() != m.columns()) {
            throw Exception("Matrix must be square to adjugate")
        }

        var colMultiplier: Int
        var rowMultiplier: Int
        return createMatrix(m.rows(), m.columns()) { r, c ->
            rowMultiplier = if (r % 2 == 0) {
                1
            } else {
                -1
            }
            colMultiplier = if (c % 2 == 0) {
                1
            } else {
                -1
            }
            val d = determinant(cofactor(m, r, c)) * colMultiplier * rowMultiplier
            d
        }
    }

    fun determinant(m: Matrix): Float {
        if (m.rows() != m.columns()) {
            throw Exception("Matrix must be square to calculate determinant")
        }

        return if (m.rows() == 1 && m.columns() == 1) {
            m[0, 0]
        } else if (m.rows() == 2 && m.columns() == 2) {
            (m[0, 0] * m[1, 1] - m[0, 1] * m[1, 0])
        } else {
            var multiplier = 1
            var sum = 0f
            for (c in 0 until m.columns()) {
                sum += m[0, c] * determinant(cofactor(m, 0, c)) * multiplier
                multiplier *= -1
            }
            sum
        }
    }

    fun cofactor(m: Matrix, r: Int, c: Int): Matrix {
        return createMatrix(m.rows() - 1, m.columns() - 1) { r1, c1 ->
            val sr = if (r1 < r) {
                r1
            } else {
                r1 + 1
            }
            val sc = if (c1 < c) {
                c1
            } else {
                c1 + 1
            }
            m[sr, sc]
        }
    }

    fun qr(m: Matrix): Pair<Matrix, Matrix> {
        val rows = m.rows()
        val cols = m.columns()

        val q = createMatrix(rows, cols) { _, _ -> 0f }
        val r = createMatrix(cols, cols) { _, _ -> 0f }

        for (j in 0 until cols) {
            var v = m.column(j)

            for (i in 0 until j) {
                val qi = q.column(i)
                r[i, j] = qi.dot(v.transpose())[0, 0]
                v = v.subtract(qi.multiply(r[i, j]))
            }

            r[j, j] = norm(v)
            q.setColumn(j, v.divide(r[j, j])[0])
        }

        return q to r
    }

    /**
     * Returns a column matrix of the diagonal of the matrix
     */
    fun diagonal(m: Matrix): Matrix {
        return createMatrix(1, min(m.rows(), m.columns())) { i, _ ->
            m[i, i]
        }
    }

    fun eigenvalues(m: Matrix, tolerance: Float = 1e-12f, maxIterations: Int = 1000): Array<Float> {
        var old = m
        var new = m

        var diff = Float.MAX_VALUE
        var iterations = 0
        while (diff > tolerance && iterations < maxIterations) {
            val (q, r) = qr(new)
            new = r.dot(q)
            diff = max(new.subtract(old).mapped { it.absoluteValue })
            old = new
            iterations++
        }

        return diagonal(new)[0]
    }

    fun norm(m: Matrix): Float {
        return sqrt(
            m.sumOf { values ->
                values.sumOf { value ->
                    value * value.toDouble()
                }
            }
        ).toFloat()
    }

    fun solveLinear(a: Matrix, b: Array<Float>): Array<Float> {
        require(a.rows() == a.columns()) { "Matrix must be square" }
        require(a.rows() == b.size) { "Matrix rows must be the same size as the vector" }

        val n = a.columns()
        val ab = a.appendColumn(b.toFloatArray())

        // Convert to row echelon form
        for (i in 0 until n) {
            var maxRow = i
            for (j in i + 1 until n) {
                if (ab[j, i].absoluteValue > ab[maxRow, i].absoluteValue) {
                    maxRow = j
                }
            }

            ab.swap(i, maxRow)

            for (j in i + 1 until n) {
                val factor = ab[j, i] / ab[i, i]
                for (k in i until n + 1) {
                    ab[j, k] -= ab[i, k] * factor
                }
            }
        }

        // Back substitution
        val x = FloatArray(n)
        for (i in n - 1 downTo 0) {
            x[i] = ab[i, n] / ab[i, i]
            for (j in i - 1 downTo 0) {
                ab[j, n] -= ab[j, i] * x[i]
            }
        }

        return x.toTypedArray()
    }

    fun leastNorm(a: Matrix, b: Array<Float>): Array<Float> {
        val (q, r) = qr(a.transpose())
        val y = q.dot(r.inverse().transpose()).dot(createMatrix(b.size, 1) { i, _ -> b[i] })
        return y.transpose()[0]
    }

    /**
     * Solves the least squares problem for the matrix equation Ax = b.
     * If A is underdetermined, the least norm solution is returned instead.
     * @param a The matrix A
     * @param b The vector b
     * @return The solution x
     */
    fun leastSquares(a: Matrix, b: Vector): Vector {
        val isUnderdetermined = a.rows() < a.columns()
        if (isUnderdetermined) {
            return leastNorm(a, b)
        }

        val jt = a.transpose()
        val jtj = jt.dot(a)
        val jtr = jt.dot(b.toColumnMatrix())
        return solveLinear(jtj, jtr.transpose()[0])
    }

    fun appendColumn(m: Matrix, col: FloatArray): Matrix {
        return createMatrix(m.rows(), m.columns() + 1) { r, c ->
            if (c < m.columns()) {
                m[r, c]
            } else {
                col[r]
            }
        }
    }

    fun appendColumn(m: Matrix, value: Float): Matrix {
        return createMatrix(m.rows(), m.columns() + 1) { r, c ->
            if (c < m.columns()) {
                m[r, c]
            } else {
                value
            }
        }
    }

    fun appendRow(m: Matrix, row: FloatArray): Matrix {
        return createMatrix(m.rows() + 1, m.columns()) { r, c ->
            if (r < m.rows()) {
                m[r, c]
            } else {
                row[c]
            }
        }
    }

    fun appendRow(m: Matrix, value: Float): Matrix {
        return createMatrix(m.rows() + 1, m.columns()) { r, c ->
            if (r < m.rows()) {
                m[r, c]
            } else {
                value
            }
        }
    }

}