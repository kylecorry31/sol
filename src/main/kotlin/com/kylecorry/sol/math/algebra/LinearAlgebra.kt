package com.kylecorry.sol.math.algebra

import com.kylecorry.sol.math.Vector
import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.statistics.Statistics
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object LinearAlgebra {

    fun dot(mat1: Matrix, mat2: Matrix): Matrix {
        require(mat1.columns() == mat2.rows()) { "Matrix 1 columns must be the same size as matrix 2 rows" }

        val product = Matrix.zeros(mat1.rows(), mat2.columns())
        check(product.rows() == mat1.rows())
        check(product.columns() == mat2.columns())
        for (r in 0..<mat1.rows()) {
            for (otherC in 0..<mat2.columns()) {
                var sum = 0.0f
                for (c in 0..<mat1.columns()) {
                    sum += mat1[r, c] * mat2[c, otherC]
                }
                check(sum.isFinite())
                product[r, otherC] = sum
            }
        }

        return product
    }

    fun dot(matrix: Matrix, vector: Vector): Matrix {
        return if (matrix.rows() == vector.size) {
            dot(matrix, vector.toRowMatrix())
        } else {
            dot(matrix, vector.toColumnMatrix())
        }
    }

    fun subtract(mat1: Matrix, mat2: Matrix): Matrix {
        require(mat1.columns() == mat2.columns() || mat2.columns() == 1) {
            "Matrix 1 columns must be the same size as matrix 2 columns"
        }

        require(mat1.rows() == mat2.rows() || mat2.rows() == 1) {
            "Matrix 1 rows must be the same size as matrix 2 rows"
        }

        return Matrix.create(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] - mat2[min(row, mat2.rows() - 1), min(col, mat2.columns() - 1)]
        }
    }

    fun subtract(mat1: Matrix, value: Float): Matrix {
        return add(mat1, -value)
    }

    fun add(mat1: Matrix, mat2: Matrix): Matrix {
        require(mat1.columns() == mat2.columns() || mat2.columns() == 1) {
            "Matrix 1 columns must be the same size as matrix 2 columns"
        }

        require(mat1.rows() == mat2.rows() || mat2.rows() == 1) {
            "Matrix 1 rows must be the same size as matrix 2 rows"
        }

        return Matrix.create(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] + mat2[min(row, mat2.rows() - 1), min(col, mat2.columns() - 1)]
        }
    }

    fun add(mat1: Matrix, value: Float): Matrix {
        return Matrix.create(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] + value
        }
    }

    fun multiply(mat1: Matrix, mat2: Matrix): Matrix {
        require(mat1.columns() == mat2.columns() || mat2.columns() == 1) {
            "Matrix 1 columns must be the same size as matrix 2 columns"
        }

        require(mat1.rows() == mat2.rows() || mat2.rows() == 1) {
            "Matrix 1 rows must be the same size as matrix 2 rows"
        }

        return Matrix.create(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] * mat2[min(row, mat2.rows() - 1), min(col, mat2.columns() - 1)]
        }
    }

    fun multiply(mat1: Matrix, scale: Float): Matrix {
        return Matrix.create(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] * scale
        }
    }

    fun divide(mat1: Matrix, mat2: Matrix): Matrix {
        require(mat1.columns() == mat2.columns() || mat2.columns() == 1) {
            "Matrix 1 columns must be the same size as matrix 2 columns"
        }

        require(mat1.rows() == mat2.rows() || mat2.rows() == 1) {
            "Matrix 1 rows must be the same size as matrix 2 rows"
        }

        return Matrix.create(mat1.rows(), mat1.columns()) { row, col ->
            mat1[row, col] / mat2[min(row, mat2.rows() - 1), min(col, mat2.columns() - 1)]
        }
    }

    fun divide(mat1: Matrix, scale: Float): Matrix {
        return multiply(mat1, 1 / scale)
    }

    fun transpose(mat: Matrix): Matrix {
        return Matrix.create(mat.columns(), mat.rows()) { row, col ->
            mat[col, row]
        }
    }

    fun map(mat: Matrix, fn: (value: Float) -> Float): Matrix {
        return Matrix.create(mat.rows(), mat.columns()) { row, col ->
            fn(mat[row, col])
        }
    }

    fun mapRows(mat: Matrix, fn: (row: FloatArray) -> FloatArray): Matrix {
        val copy = mat.clone()
        val temp = FloatArray(mat.columns())
        for (row in 0..<mat.rows()) {
            mat.getRow(row, temp)
            copy.setRow(row, fn(temp))
        }
        return copy
    }

    fun mapColumns(mat: Matrix, fn: (row: FloatArray) -> FloatArray): Matrix {
        return mapRows(mat.transpose(), fn).transpose()
    }

    fun sum(mat: Matrix): Float {
        return mat.sum()
    }

    fun sumColumns(mat: Matrix): Matrix {
        return sumRows(mat.transpose()).transpose()
    }

    fun sumRows(mat: Matrix): Matrix {
        val temp = FloatArray(mat.columns())
        return Matrix.create(mat.rows(), 1) { row, _ ->
            mat.getRow(row, temp)
            temp.sum()
        }
    }

    fun max(mat: Matrix): Float {
        return mat.max()
    }

    fun maxColumns(mat: Matrix): Matrix {
        return maxRows(mat.transpose()).transpose()
    }

    fun maxRows(mat: Matrix): Matrix {
        val temp = FloatArray(mat.columns())
        return Matrix.create(mat.rows(), 1) { row, _ ->
            mat.getRow(row, temp)
            temp.max()
        }
    }

    fun inverse(m: Matrix): Matrix {
        require(m.rows() == m.columns()) {
            "Matrix must be square to calculate inverse"
        }

        val det = determinant(m)
        if (Arithmetic.isZero(det)) {
            // No inverse exists
            return Matrix.zeros(m.rows(), m.columns())
        }
        return adjugate(m).transpose().divide(determinant(m))
    }

    fun adjugate(m: Matrix): Matrix {
        require(m.rows() == m.columns()) {
            "Matrix must be square to calculate adjugate"
        }

        var colMultiplier: Int
        var rowMultiplier: Int
        return Matrix.create(m.rows(), m.columns()) { r, c ->
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
        require(m.rows() == m.columns()) {
            "Matrix must be square to calculate determinant"
        }

        return if (m.rows() == 1 && m.columns() == 1) {
            m[0, 0]
        } else if (m.rows() == 2 && m.columns() == 2) {
            (m[0, 0] * m[1, 1] - m[0, 1] * m[1, 0])
        } else {
            var multiplier = 1
            var sum = 0f
            for (c in 0..<m.columns()) {
                sum += m[0, c] * determinant(cofactor(m, 0, c)) * multiplier
                multiplier *= -1
            }
            sum
        }
    }

    fun cofactor(m: Matrix, r: Int, c: Int): Matrix {
        require(m.rows() > 0)
        require(m.columns() > 0)

        return Matrix.create(m.rows() - 1, m.columns() - 1) { r1, c1 ->
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

        val q = Matrix.zeros(rows, cols)
        val r = Matrix.zeros(cols, cols)

        for (j in 0..<cols) {
            var v = Matrix.row(values = m.getColumn(j))

            for (i in 0..<j) {
                val qi = Matrix.row(values = q.getColumn(i))
                r[i, j] = qi.dot(v.transpose())[0, 0]
                v = v.subtract(qi.multiply(r[i, j]))
            }

            r[j, j] = norm(v)
            if (Arithmetic.isZero(r[j, j])) {
                q.setColumn(j, orthogonalUnitVector(q, j))
            } else {
                q.setColumn(j, v.divide(r[j, j]).getRow(0))
            }
        }

        return q to r
    }

    /**
     * Returns a column matrix of the diagonal of the matrix
     */
    fun diagonal(m: Matrix): Matrix {
        return Matrix.create(1, min(m.rows(), m.columns())) { _, i ->
            m[i, i]
        }
    }

    fun eigen(
        m: Matrix,
        tolerance: Float = 1e-12f,
        maxIterations: Int = 1000
    ): EigenDecomposition {
        require(m.rows() == m.columns()) { "Matrix must be square" }
        require(m.rows() > 0) { "Matrix must not be empty" }
        require(tolerance > 0f) { "Tolerance must be greater than zero" }
        require(maxIterations >= 0) { "Max iterations must be non-negative" }

        var old = m
        var new = m
        var vectors = Matrix.identity(m.rows())

        var diff = Float.MAX_VALUE
        var iterations = 0
        while (diff > tolerance && iterations < maxIterations) {
            val (q, r) = qr(new)
            new = r.dot(q)
            vectors = vectors.dot(q)
            diff = max(new.subtract(old).mapped { it.absoluteValue })
            old = new
            iterations++
        }

        return EigenDecomposition(Vector(diagonal(new).getRow(0)), normalizeEigenvectorSigns(vectors))
    }

    fun norm(m: Matrix): Float {
        return m.norm()
    }

    fun solveLinear(a: Matrix, b: Vector): Vector {
        require(a.rows() == a.columns()) { "Matrix must be square" }
        require(a.rows() == b.n) { "Matrix rows must be the same size as the vector" }

        val n = a.columns()
        val ab = a.appendColumn(b.data)

        // Convert to row echelon form
        for (i in 0..<n) {
            var maxRow = i
            for (j in i + 1..<n) {
                if (ab[j, i].absoluteValue > ab[maxRow, i].absoluteValue) {
                    maxRow = j
                }
            }

            ab.swapRows(i, maxRow)

            for (j in i + 1..<n) {
                val factor = ab[j, i] / ab[i, i]
                for (k in i..<n + 1) {
                    ab[j, k] -= ab[i, k] * factor
                }
            }
        }

        // Back substitution
        val x = Vector.create(n)
        for (i in n - 1 downTo 0) {
            x[i] = ab[i, n] / ab[i, i]
            for (j in i - 1 downTo 0) {
                ab[j, n] -= ab[j, i] * x[i]
            }
        }

        return x
    }

    fun leastNorm(a: Matrix, b: Vector): Vector {
        val (q, r) = qr(a.transpose())
        val y = q.dot(r.inverse().transpose()).dot(b.toColumnMatrix())
        return Vector(y.getColumn(0))
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
        return solveLinear(jtj, Vector(jtr.getColumn(0)))
    }

    /**
     * Solves the weighted least squares problem for the matrix equation Ax = b.
     * If A is underdetermined, the least norm solution is returned instead.
     * @param a The matrix A
     * @param b The vector b
     * @param weights The row weights
     * @return The solution x
     */
    fun weightedLeastSquares(a: Matrix, b: Vector, weights: Vector): Vector {
        require(weights.size == a.rows()) { "Weights must have the same size as the number of rows in A" }
        require(b.size == a.rows()) { "B must have the same size as the number of rows in A" }
        require(weights.data.all { it >= 0f }) { "Weights must be greater than or equal to 0" }

        val weightedA = Matrix.create(a.rows(), a.columns()) { row, col ->
            a[row, col] * sqrt(weights[row])
        }
        val weightedB = Vector(FloatArray(b.size) { index ->
            b[index] * sqrt(weights[index])
        })

        val isUnderdetermined = weightedA.rows() < weightedA.columns()
        return if (isUnderdetermined) {
            leastNorm(weightedA, weightedB)
        } else {
            leastSquares(weightedA, weightedB)
        }
    }

    /**
     * Solves the robust least squares problem for the matrix equation Ax = b using Huber weights.
     * This is iteratively reweighted least squares, which reduces the influence of outliers.
     * @param a The matrix A
     * @param b The vector b
     * @param tuningConstant The Huber tuning constant applied to the residual scale
     * @param minScale The minimum residual scale
     * @param maxIterations The maximum number of reweighting iterations
     * @return The solution x
     */
    fun robustLeastSquares(
        a: Matrix,
        b: Vector,
        tuningConstant: Float = 1.5f,
        minScale: Float = 1e-5f,
        maxIterations: Int = 10
    ): Vector {
        require(b.size == a.rows()) { "B must have the same size as the number of rows in A" }
        require(tuningConstant > 0f) { "Tuning constant must be greater than 0" }
        require(minScale > 0f) { "Minimum scale must be greater than 0" }
        require(maxIterations >= 0) { "Maximum iterations must be greater than or equal to 0" }

        var weights = Vector(FloatArray(a.rows()) { 1f })
        var solution = weightedLeastSquares(a, b, weights)

        repeat(maxIterations) {
            val residuals = getResiduals(a, b, solution)
            val scale = max(
                minScale,
                Statistics.medianAbsoluteDeviationToStandardDeviation(
                    Statistics.medianAbsoluteDeviation(
                        residuals.data.toList(),
                        0f
                    )
                )
            )
            weights = Vector(FloatArray(a.rows()) { index ->
                Statistics.huberWeight(residuals[index], scale * tuningConstant)
            })
            solution = weightedLeastSquares(a, b, weights)
        }

        return solution
    }

    fun appendColumn(m: Matrix, col: FloatArray): Matrix {
        require(col.size == m.rows())
        return Matrix.create(m.rows(), m.columns() + 1) { r, c ->
            if (c < m.columns()) {
                m[r, c]
            } else {
                col[r]
            }
        }
    }

    private fun getResiduals(a: Matrix, b: Vector, x: Vector): Vector {
        return dot(a, x).toVector() - b
    }

    private fun orthogonalUnitVector(q: Matrix, column: Int): FloatArray {
        for (attempt in 0..<q.rows()) {
            val vector = FloatArray(q.rows()) { index ->
                if (index == (column + attempt) % q.rows()) 1f else 0f
            }

            for (previous in 0..<column) {
                var projection = 0f
                for (row in 0..<q.rows()) {
                    projection += vector[row] * q[row, previous]
                }

                for (row in 0..<q.rows()) {
                    vector[row] -= projection * q[row, previous]
                }
            }

            val norm = sqrt(vector.sumOf { it * it.toDouble() }).toFloat()
            if (!Arithmetic.isZero(norm)) {
                return FloatArray(vector.size) { index -> vector[index] / norm }
            }
        }

        return FloatArray(q.rows()) { index -> if (index == column % q.rows()) 1f else 0f }
    }

    private fun normalizeEigenvectorSigns(vectors: Matrix): Matrix {
        return Matrix.create(vectors.rows(), vectors.columns()) { row, column ->
            var maxIndex = 0
            for (i in 1..<vectors.rows()) {
                if (vectors[i, column].absoluteValue > vectors[maxIndex, column].absoluteValue) {
                    maxIndex = i
                }
            }

            vectors[row, column] * if (vectors[maxIndex, column] >= 0f) 1f else -1f
        }
    }

    fun appendColumn(m: Matrix, value: Float): Matrix {
        return Matrix.create(m.rows(), m.columns() + 1) { r, c ->
            if (c < m.columns()) {
                m[r, c]
            } else {
                value
            }
        }
    }

    fun appendRow(m: Matrix, row: FloatArray): Matrix {
        require(row.size == m.columns())
        return Matrix.create(m.rows() + 1, m.columns()) { r, c ->
            if (r < m.rows()) {
                m[r, c]
            } else {
                row[c]
            }
        }
    }

    fun appendRow(m: Matrix, value: Float): Matrix {
        return Matrix.create(m.rows() + 1, m.columns()) { r, c ->
            if (r < m.rows()) {
                m[r, c]
            } else {
                value
            }
        }
    }

    data class EigenDecomposition(val values: Vector, val vectors: Matrix)

}
