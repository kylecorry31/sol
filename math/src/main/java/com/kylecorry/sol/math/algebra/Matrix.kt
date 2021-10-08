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