package com.kylecorry.sol.math.algebra

typealias Matrix = Array<Array<Float>>

fun Matrix.rows(): Int {
    return size
}

fun Matrix.columns(): Int {
    if (rows() == 0) {
        return 0
    }

    return get(0).size
}

operator fun Matrix.get(row: Int, column: Int): Float {
    return this[row][column]
}

operator fun Matrix.set(row: Int, column: Int, value: Float) {
    this[row][column] = value
}

fun createMatrix(rows: Int, columns: Int, init: (row: Int, col: Int) -> Float): Matrix {
    return Array(rows) { row ->
        Array(columns) { col ->
            init(row, col)
        }
    }
}

fun createMatrix(rows: Int, columns: Int, init: Float): Matrix {
    return createMatrix(rows, columns) { _, _ -> init }
}

fun Matrix.dot(other: Matrix): Matrix {
    return LinearAlgebra.dot(this, other)
}

fun Matrix.add(other: Matrix): Matrix {
    return LinearAlgebra.add(this, other)
}

fun Matrix.subtract(other: Matrix): Matrix {
    return LinearAlgebra.subtract(this, other)
}

fun Matrix.multiply(other: Matrix): Matrix {
    return LinearAlgebra.multiply(this, other)
}

fun Matrix.multiply(scale: Float): Matrix {
    return LinearAlgebra.multiply(this, scale)
}

fun Matrix.divide(other: Matrix): Matrix {
    return LinearAlgebra.divide(this, other)
}

fun Matrix.divide(scale: Float): Matrix {
    return LinearAlgebra.divide(this, scale)
}

fun Matrix.transpose(): Matrix {
    return LinearAlgebra.transpose(this)
}

fun Matrix.mapped(fn: (Float) -> Float): Matrix {
    return LinearAlgebra.map(this, fn)
}

fun Matrix.mapRows(fn: (FloatArray) -> FloatArray): Matrix {
    return LinearAlgebra.mapRows(this, fn)
}

fun Matrix.mapColumns(fn: (FloatArray) -> FloatArray): Matrix {
    return LinearAlgebra.mapColumns(this, fn)
}

fun Matrix.sum(): Float {
    return LinearAlgebra.sum(this)
}

fun Matrix.sumRows(): Matrix {
    return LinearAlgebra.sumRows(this)
}

fun Matrix.sumColumns(): Matrix {
    return LinearAlgebra.sumColumns(this)
}

fun Matrix.max(): Float {
    return LinearAlgebra.max(this)
}

fun Matrix.maxRows(): Matrix {
    return LinearAlgebra.maxRows(this)
}

fun Matrix.maxColumns(): Matrix {
    return LinearAlgebra.maxColumns(this)
}

fun Matrix.inverse(): Matrix {
    return LinearAlgebra.inverse(this)
}

fun Matrix.adjugate(): Matrix {
    return LinearAlgebra.adjugate(this)
}

fun Matrix.det(): Float {
    return LinearAlgebra.determinant(this)
}

fun Matrix.cofactor(r: Int, c: Int): Matrix {
    return LinearAlgebra.cofactor(this, r, c)
}

fun Matrix.appendColumn(col: FloatArray): Matrix {
    return LinearAlgebra.appendColumn(this, col)
}

fun Matrix.appendColumn(value: Float): Matrix {
    return LinearAlgebra.appendColumn(this, value)
}

fun Matrix.appendRow(row: FloatArray): Matrix {
    return LinearAlgebra.appendRow(this, row)
}

fun Matrix.appendRow(value: Float): Matrix {
    return LinearAlgebra.appendRow(this, value)
}

/**
 * Creates an identity matrix
 */
fun identityMatrix(size: Int): Matrix {
    return diagonalMatrix(values = FloatArray(size) { 1f })
}

fun diagonalMatrix(vararg values: Float): Matrix {
    return createMatrix(values.size, values.size) { r, c -> if (r == c) values[r] else 0f }
}

/**
 * Creates a column matrix
 */
fun columnMatrix(vararg values: Float): Matrix {
    return createMatrix(values.size, 1) { r, _ -> values[r] }
}

/**
 * Creates a row matrix
 */
fun rowMatrix(vararg values: Float): Matrix {
    return createMatrix(1, values.size) { _, c -> values[c] }
}