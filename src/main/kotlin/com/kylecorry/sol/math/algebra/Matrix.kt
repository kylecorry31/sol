package com.kylecorry.sol.math.algebra

class Matrix private constructor(val rows: Int, val columns: Int, internal val data: FloatArray) {

    operator fun get(row: Int, column: Int): Float {
        return data[getIndex(row, column)]
    }

    operator fun set(row: Int, column: Int, value: Float) {
        data[getIndex(row, column)] = value
    }

    fun rows(): Int {
        return rows
    }

    fun columns(): Int {
        return columns
    }

    fun clone(): Matrix {
        return create(rows, columns, data.clone())
    }

    fun setRow(row: Int, rowData: FloatArray) {
        if (rowData.size != columns()) {
            throw IllegalArgumentException("Expected row to be of length ${columns()} but got ${rowData.size}")
        }
        val startIndex = getIndex(row, 0)
        rowData.copyInto(data, startIndex)
    }

    fun getRow(row: Int, destination: FloatArray = FloatArray(columns())): FloatArray {
        if (destination.size != columns()) {
            throw IllegalArgumentException("Expected destination to be of length ${columns()} but got ${destination.size}")
        }
        val startIndex = getIndex(row, 0)
        data.copyInto(destination, startIndex = startIndex, endIndex = startIndex + columns())
        return destination
    }

    fun setColumn(column: Int, columnData: FloatArray) {
        if (columnData.size != rows()) {
            throw IllegalArgumentException("Expected column to be of length ${rows()} but got ${columnData.size}")
        }
        for (row in 0 until rows()) {
            set(row, column, columnData[row])
        }
    }

    fun getColumn(column: Int, destination: FloatArray = FloatArray(rows())): FloatArray {
        if (destination.size != rows()) {
            throw IllegalArgumentException("Expected destination to be of length ${rows()} but got ${destination.size}")
        }
        for (row in 0 until rows()) {
            destination[row] = get(row, column)
        }
        return destination
    }

    fun swapRows(row1: Int, row2: Int) {
        val temp = getRow(row1)
        setRow(row1, getRow(row2))
        setRow(row2, temp)
    }

    private fun getIndex(row: Int, column: Int): Int {
        if (row >= rows() || row < 0) {
            throw IllegalArgumentException("Expected row to be between 0 and ${rows()} but got $row")
        }
        if (column >= columns() || columns < 0) {
            throw IllegalArgumentException("Expected column to be between 0 and ${columns()} but got $column")
        }
        return columns * row + column
    }

    companion object {
        fun zeros(rows: Int, columns: Int): Matrix {
            return create(rows, columns, FloatArray(rows * columns))
        }

        fun create(rows: Int, columns: Int, value: Float = 0f): Matrix {
            return create(rows, columns, FloatArray(rows * columns) { value })
        }

        fun create(rows: Int, columns: Int, data: FloatArray): Matrix {
            if (data.size != rows * columns) {
                throw IllegalArgumentException("Expected data to be of length ${rows * columns} but got ${data.size}")
            }
            return Matrix(rows, columns, data)
        }

        fun identity(size: Int): Matrix {
            return diagonal(values = FloatArray(size) { 1f })
        }

        fun diagonal(vararg values: Float): Matrix {
            return create(values.size, values.size) { r, c -> if (r == c) values[r] else 0f }
        }

        fun column(vararg values: Float): Matrix {
            return create(values.size, 1) { r, _ -> values[r] }
        }

        fun row(vararg values: Float): Matrix {
            return create(1, values.size) { _, c -> values[c] }
        }

        fun create(oldMatrix: Array<Array<Float>>): Matrix {
            return create(oldMatrix.size, oldMatrix[0].size) { r, c ->
                oldMatrix[r][c]
            }
        }

        inline fun create(rows: Int, columns: Int, crossinline initialize: (row: Int, column: Int) -> Float): Matrix {
            return create(rows, columns, FloatArray(rows * columns) { i ->
                val row = i / columns
                val column = i % columns
                initialize(row, column)
            })
        }
    }
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

fun Matrix.norm(): Float {
    return LinearAlgebra.norm(this)
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