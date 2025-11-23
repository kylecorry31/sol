package com.kylecorry.sol.math.algebra

import kotlin.math.sqrt

@JvmInline
value class Matrix internal constructor(private val rawData: FloatArray) {

    val data: FloatArray
        get() = rawData.copyOfRange(2, rawData.size)

    operator fun get(row: Int, column: Int): Float {
        return rawData[getIndex(row, column)]
    }

    operator fun set(row: Int, column: Int, value: Float) {
        rawData[getIndex(row, column)] = value
    }

    fun sum(): Float {
        var sum = 0.0f
        for (i in 2..rawData.lastIndex) {
            sum += rawData[i]
        }
        return sum
    }

    fun max(): Float {
        if (rawData.size <= 2) {
            return 0f
        }
        var max = rawData[2]
        for (i in 3..rawData.lastIndex) {
            max = maxOf(max, rawData[i])
        }
        return max
    }

    fun norm(): Float {
        return sqrt(rawData.sumOf { it * it.toDouble() }).toFloat()
    }

    fun rows(): Int {
        return rawData[0].toRawBits()
    }

    fun columns(): Int {
        return rawData[1].toRawBits()
    }

    fun clone(): Matrix {
        return Matrix(rawData.clone())
    }

    fun setRow(row: Int, rowData: FloatArray) {
        if (rowData.size != columns()) {
            throw IllegalArgumentException("Expected row to be of length ${columns()} but got ${rowData.size}")
        }
        val startIndex = getIndex(row, 0)
        rowData.copyInto(rawData, startIndex)
    }

    fun getRow(row: Int, destination: FloatArray = FloatArray(columns())): FloatArray {
        if (destination.size != columns()) {
            throw IllegalArgumentException("Expected destination to be of length ${columns()} but got ${destination.size}")
        }
        val startIndex = getIndex(row, 0)
        rawData.copyInto(destination, startIndex = startIndex, endIndex = startIndex + columns())
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
        if (column >= columns() || column < 0) {
            throw IllegalArgumentException("Expected column to be between 0 and ${columns()} but got $column")
        }
        return 2 + columns() * row + column
    }

    companion object {
        fun zeros(rows: Int, columns: Int): Matrix {
            val data = FloatArray(rows * columns + 2)
            data[0] = Float.fromBits(rows)
            data[1] = Float.fromBits(columns)
            return Matrix(data)
        }

        fun create(rows: Int, columns: Int, value: Float = 0f): Matrix {
            val data = FloatArray(rows * columns + 2) { value }
            data[0] = Float.fromBits(rows)
            data[1] = Float.fromBits(columns)
            return Matrix(data)
        }

        fun createFromRawData(data: FloatArray): Matrix {
            return Matrix(data)
        }

        fun create(rows: Int, columns: Int, data: FloatArray): Matrix {
            if (data.size != rows * columns) {
                throw IllegalArgumentException("Expected data to be of length ${rows * columns} but got ${data.size}")
            }
            val newData = FloatArray(2 + data.size)
            newData[0] = Float.fromBits(rows)
            newData[1] = Float.fromBits(columns)
            data.copyInto(newData, 2)
            return Matrix(newData)
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

        inline fun create(
            rows: Int,
            columns: Int,
            crossinline initialize: (row: Int, column: Int) -> Float
        ): Matrix {
            val newData = FloatArray(2 + rows * columns)
            newData[0] = Float.fromBits(rows)
            newData[1] = Float.fromBits(columns)
            for (i in 2 until newData.size) {
                val index = i - 2
                val row = index / columns
                val column = index % columns
                newData[i] = initialize(row, column)
            }
            return createFromRawData(newData)
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

fun Matrix.sumRows(): Matrix {
    return LinearAlgebra.sumRows(this)
}

fun Matrix.sumColumns(): Matrix {
    return LinearAlgebra.sumColumns(this)
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