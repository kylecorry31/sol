package com.kylecorry.sol.math.algebra

import kotlin.math.abs
import kotlin.math.sqrt

internal object SymmetricEigen {

    /**
     * @param tolerance stop once the squared off diagonal values are this fraction of the squared matrix norm
     * @return the eigenvalues and the eigenvectors (eigenvectors[i] belongs to eigenvalues[i])
     */
    fun decompose(
        matrix: Array<DoubleArray>,
        maxSweeps: Int = 50,
        tolerance: Double = 1e-24
    ): Pair<DoubleArray, Array<DoubleArray>> {
        val size = matrix.size
        val a = Array(size) { matrix[it].copyOf() }
        val v = Array(size) { i -> DoubleArray(size) { j -> if (i == j) 1.0 else 0.0 } }
        val normSquared = matrix.sumOf { row -> row.sumOf { it * it } }
        var sweep = 0
        while (sweep < maxSweeps && offDiagonalSquared(a) > tolerance * normSquared) {
            for (p in 0 until size) {
                for (q in p + 1 until size) {
                    rotate(a, v, p, q)
                }
            }
            sweep++
        }
        val values = DoubleArray(size) { a[it][it] }
        val vectors = Array(size) { column -> DoubleArray(size) { row -> v[row][column] } }
        return values to vectors
    }

    fun largestEigenvector(matrix: Array<DoubleArray>): DoubleArray {
        val (values, vectors) = decompose(matrix)
        return vectors[values.indices.maxBy { values[it] }]
    }

    private fun offDiagonalSquared(a: Array<DoubleArray>): Double {
        var sum = 0.0
        for (p in a.indices) {
            for (q in p + 1 until a.size) {
                sum += a[p][q] * a[p][q]
            }
        }
        return sum
    }

    private fun rotate(a: Array<DoubleArray>, v: Array<DoubleArray>, p: Int, q: Int) {
        if (abs(a[p][q]) < Double.MIN_VALUE) {
            return
        }
        val theta = (a[q][q] - a[p][p]) / (2 * a[p][q])
        val t = (if (theta >= 0) 1.0 else -1.0) / (abs(theta) + sqrt(theta * theta + 1))
        val c = 1 / sqrt(t * t + 1)
        val s = t * c
        val size = a.size
        for (k in 0 until size) {
            val akp = a[k][p]
            val akq = a[k][q]
            a[k][p] = c * akp - s * akq
            a[k][q] = s * akp + c * akq
        }
        for (k in 0 until size) {
            val apk = a[p][k]
            val aqk = a[q][k]
            a[p][k] = c * apk - s * aqk
            a[q][k] = s * apk + c * aqk
        }
        for (k in 0 until size) {
            val vkp = v[k][p]
            val vkq = v[k][q]
            v[k][p] = c * vkp - s * vkq
            v[k][q] = s * vkp + c * vkq
        }
    }
}
