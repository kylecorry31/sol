package com.kylecorry.sol.math.algebra

typealias Matrix = Array<Array<Float>>

val service = LinearAlgebraService()

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
    return service.dot(this, other)
}

fun Matrix.add(other: Matrix): Matrix {
    return service.add(this, other)
}

fun Matrix.subtract(other: Matrix): Matrix {
    return service.subtract(this, other)
}

fun Matrix.multiply(other: Matrix): Matrix {
    return service.multiply(this, other)
}

fun Matrix.multiply(scale: Float): Matrix {
    return service.multiply(this, scale)
}

fun Matrix.divide(other: Matrix): Matrix {
    return service.divide(this, other)
}

fun Matrix.divide(scale: Float): Matrix {
    return service.divide(this, scale)
}

fun Matrix.transpose(): Matrix {
    return service.transpose(this)
}

fun Matrix.mapped(fn: (Float) -> Float): Matrix {
    return service.map(this, fn)
}

fun Matrix.mapRows(fn: (FloatArray) -> FloatArray): Matrix {
    return service.mapRows(this, fn)
}

fun Matrix.mapColumns(fn: (FloatArray) -> FloatArray): Matrix {
    return service.mapColumns(this, fn)
}

fun Matrix.sum(): Float {
    return service.sum(this)
}

fun Matrix.sumRows(): Matrix {
    return service.sumRows(this)
}

fun Matrix.sumColumns(): Matrix {
    return service.sumColumns(this)
}

fun Matrix.max(): Float {
    return service.max(this)
}

fun Matrix.maxRows(): Matrix {
    return service.maxRows(this)
}

fun Matrix.maxColumns(): Matrix {
    return service.maxColumns(this)
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