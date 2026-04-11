package com.kylecorry.sol.math.algebra

import kotlin.math.sqrt

@JvmInline
value class Matrix internal constructor(
    private val rawData: FloatArray,
) {
    operator fun get(
        row: Int,
        column: Int,
    ): Float = rawData[getIndex(row, column)]

    operator fun set(
        row: Int,
        column: Int,
        value: Float,
    ) {
        rawData[getIndex(row, column)] = value
    }

    fun sum(): Float {
        var sum = 0.0
        for (i in 2..rawData.lastIndex) {
            sum += rawData[i]
        }
        return sum.toFloat()
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
        var sum = 0.0
        for (i in 2..rawData.lastIndex) {
            sum += rawData[i] * rawData[i].toDouble()
        }
        check(sum >= 0) { "Sum of squares must be non-negative but was $sum" }
        return sqrt(sum.toFloat())
    }

    fun rows(): Int = rawData[0].toRawBits()

    fun columns(): Int = rawData[1].toRawBits()

    fun clone(): Matrix = Matrix(rawData.clone())

    fun setRow(
        row: Int,
        rowData: FloatArray,
    ) {
        require(rowData.size == columns()) {
            "Expected row to be of length ${columns()} but got ${rowData.size}"
        }
        val startIndex = getIndex(row, 0)
        rowData.copyInto(rawData, startIndex)
    }

    fun getRow(
        row: Int,
        destination: FloatArray = FloatArray(columns()),
    ): FloatArray {
        require(destination.size == columns()) {
            "Expected destination to be of length ${columns()} but got ${destination.size}"
        }
        val startIndex = getIndex(row, 0)
        rawData.copyInto(destination, startIndex = startIndex, endIndex = startIndex + columns())
        return destination
    }

    fun setColumn(
        column: Int,
        columnData: FloatArray,
    ) {
        require(columnData.size == rows()) {
            "Expected column to be of length ${rows()} but got ${columnData.size}"
        }
        for (row in 0..<rows()) {
            set(row, column, columnData[row])
        }
    }

    fun getColumn(
        column: Int,
        destination: FloatArray = FloatArray(rows()),
    ): FloatArray {
        require(destination.size == rows()) {
            "Expected destination to be of length ${rows()} but got ${destination.size}"
        }
        for (row in 0..<rows()) {
            destination[row] = get(row, column)
        }
        return destination
    }

    fun swapRows(
        row1: Int,
        row2: Int,
    ) {
        val temp = getRow(row1)
        setRow(row1, getRow(row2))
        setRow(row2, temp)
    }

    private fun getIndex(
        row: Int,
        column: Int,
    ): Int {
        require(row in 0..<rows()) {
            "Expected row to be between 0 and ${rows()} but got $row"
        }
        require(column in 0..<columns()) {
            "Expected column to be between 0 and ${columns()} but got $column"
        }
        return 2 + columns() * row + column
    }

    companion object {
        fun zeros(
            rows: Int,
            columns: Int,
        ): Matrix {
            require(rows >= 0) { "rows must be non-negative, but was $rows" }
            require(columns >= 0) { "columns must be non-negative, but was $columns" }
            val data = FloatArray(rows * columns + 2)
            data[0] = Float.fromBits(rows)
            data[1] = Float.fromBits(columns)
            val matrix = Matrix(data)
            check(matrix.rows() == rows) { "The matrix doesn't have the right number of rows" }
            check(matrix.columns() == columns) { "The matrix doesn't have the right number of columns" }
            return matrix
        }

        fun create(
            rows: Int,
            columns: Int,
            value: Float = 0f,
        ): Matrix {
            require(rows >= 0) { "rows must be non-negative, but was $rows" }
            require(columns >= 0) { "columns must be non-negative, but was $columns" }
            val data = FloatArray(rows * columns + 2) { value }
            data[0] = Float.fromBits(rows)
            data[1] = Float.fromBits(columns)
            val matrix = Matrix(data)
            check(matrix.rows() == rows) { "The matrix doesn't have the right number of rows" }
            check(matrix.columns() == columns) { "The matrix doesn't have the right number of columns" }
            return matrix
        }

        fun createFromRawData(data: FloatArray): Matrix = Matrix(data)

        fun create(
            rows: Int,
            columns: Int,
            data: FloatArray,
        ): Matrix {
            require(rows >= 0) { "rows must be non-negative, but was $rows" }
            require(columns >= 0) { "columns must be non-negative, but was $columns" }
            require(data.size == rows * columns) {
                "Expected data to be of length ${rows * columns} but got ${data.size}"
            }
            val newData = FloatArray(2 + data.size)
            newData[0] = Float.fromBits(rows)
            newData[1] = Float.fromBits(columns)
            data.copyInto(newData, 2)
            val matrix = Matrix(newData)
            check(matrix.rows() == rows) { "The matrix doesn't have the right number of rows" }
            check(matrix.columns() == columns) { "The matrix doesn't have the right number of columns" }
            return matrix
        }

        fun identity(size: Int): Matrix {
            require(size >= 0) { "size must be non-negative, but was $size" }
            val matrix = diagonal(values = FloatArray(size) { 1f })
            check(matrix.rows() == size) { "The matrix doesn't have the right number of rows" }
            check(matrix.columns() == size) { "The matrix doesn't have the right number of columns" }
            return matrix
        }

        fun diagonal(vararg values: Float): Matrix {
            val matrix = create(values.size, values.size) { r, c -> if (r == c) values[r] else 0f }
            check(matrix.rows() == values.size) { "The matrix doesn't have the right number of rows" }
            check(matrix.columns() == values.size) { "The matrix doesn't have the right number of columns" }
            return matrix
        }

        fun column(vararg values: Float): Matrix {
            val matrix = create(values.size, 1) { r, _ -> values[r] }
            check(matrix.rows() == values.size) { "The matrix doesn't have the right number of rows" }
            check(matrix.columns() == 1) { "The matrix doesn't have the right number of columns" }
            return matrix
        }

        fun row(vararg values: Float): Matrix {
            val matrix = create(1, values.size) { _, c -> values[c] }
            check(matrix.rows() == 1) { "The matrix doesn't have the right number of rows" }
            check(matrix.columns() == values.size) { "The matrix doesn't have the right number of columns" }
            return matrix
        }

        fun create(oldMatrix: Array<Array<Float>>): Matrix {
            val rows = oldMatrix.size
            val columns = oldMatrix.getOrNull(0)?.size ?: 0
            val matrix =
                create(rows, columns) { r, c ->
                    oldMatrix[r][c]
                }
            check(matrix.rows() == rows) { "The matrix doesn't have the right number of rows" }
            check(matrix.columns() == columns) { "The matrix doesn't have the right number of columns" }
            return matrix
        }

        inline fun create(
            rows: Int,
            columns: Int,
            crossinline initialize: (row: Int, column: Int) -> Float,
        ): Matrix {
            require(rows >= 0) { "rows must be non-negative, but was $rows" }
            require(columns >= 0) { "columns must be non-negative, but was $columns" }
            val newData = FloatArray(2 + rows * columns)
            newData[0] = Float.fromBits(rows)
            newData[1] = Float.fromBits(columns)
            for (i in 2..<newData.size) {
                val index = i - 2
                val row = index / columns
                val column = index % columns
                require(row <= rows) { "Row $row out of range $rows" }
                require(column <= columns) { "Column $column out of range $columns" }
                newData[i] = initialize(row, column)
            }
            val matrix = createFromRawData(newData)
            check(matrix.rows() == rows) { "The matrix doesn't have the right number of rows" }
            check(matrix.columns() == columns) { "The matrix doesn't have the right number of columns" }
            return matrix
        }
    }
}

fun Matrix.dot(other: Matrix): Matrix = LinearAlgebra.dot(this, other)

fun Matrix.add(other: Matrix): Matrix = LinearAlgebra.add(this, other)

fun Matrix.subtract(other: Matrix): Matrix = LinearAlgebra.subtract(this, other)

fun Matrix.multiply(other: Matrix): Matrix = LinearAlgebra.multiply(this, other)

fun Matrix.multiply(scale: Float): Matrix = LinearAlgebra.multiply(this, scale)

fun Matrix.divide(other: Matrix): Matrix = LinearAlgebra.divide(this, other)

fun Matrix.divide(scale: Float): Matrix = LinearAlgebra.divide(this, scale)

fun Matrix.transpose(): Matrix = LinearAlgebra.transpose(this)

fun Matrix.mapped(fn: (Float) -> Float): Matrix = LinearAlgebra.map(this, fn)

fun Matrix.mapRows(fn: (FloatArray) -> FloatArray): Matrix = LinearAlgebra.mapRows(this, fn)

fun Matrix.mapColumns(fn: (FloatArray) -> FloatArray): Matrix = LinearAlgebra.mapColumns(this, fn)

fun Matrix.sumRows(): Matrix = LinearAlgebra.sumRows(this)

fun Matrix.sumColumns(): Matrix = LinearAlgebra.sumColumns(this)

fun Matrix.maxRows(): Matrix = LinearAlgebra.maxRows(this)

fun Matrix.maxColumns(): Matrix = LinearAlgebra.maxColumns(this)

fun Matrix.inverse(): Matrix = LinearAlgebra.inverse(this)

fun Matrix.adjugate(): Matrix = LinearAlgebra.adjugate(this)

fun Matrix.det(): Float = LinearAlgebra.determinant(this)

fun Matrix.cofactor(
    r: Int,
    c: Int,
): Matrix = LinearAlgebra.cofactor(this, r, c)

fun Matrix.appendColumn(col: FloatArray): Matrix = LinearAlgebra.appendColumn(this, col)

fun Matrix.appendColumn(value: Float): Matrix = LinearAlgebra.appendColumn(this, value)

fun Matrix.appendRow(row: FloatArray): Matrix = LinearAlgebra.appendRow(this, row)

fun Matrix.appendRow(value: Float): Matrix = LinearAlgebra.appendRow(this, value)
