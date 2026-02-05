package com.kylecorry.sol.math.lists

import com.kylecorry.sol.math.Vector2

object Lists {

    fun <T : Comparable<T>> argmax(values: List<T>): Int {
        if (values.isEmpty()) {
            return -1
        }

        var maxIndex = 0

        for (i in values.indices) {
            if (values[i] > values[maxIndex]) {
                maxIndex = i
            }
        }

        return maxIndex
    }

    fun <T : Comparable<T>> argmin(values: List<T>): Int {
        if (values.isEmpty()) {
            return -1
        }

        var minIndex = 0

        for (i in values.indices) {
            if (values[i] < values[minIndex]) {
                minIndex = i
            }
        }

        return minIndex
    }

    fun <T> oneHot(value: Int, classes: Int, on: T, off: T): List<T> {
        return List(classes) { if (it == value) on else off }
    }

    fun isIncreasingX(data: List<Vector2>): Boolean {
        for (i in 1 until data.size) {
            if (data[i].x < data[i - 1].x) {
                return false
            }
        }
        return true
    }

    fun <T> reorder(data: List<T>, indices: List<Int>, inverse: Boolean = false): List<T> {
        return if (inverse) {
            val newIndices = MutableList(indices.size) { it }
            for (i in indices.indices) {
                val index = indices[i]
                newIndices[index] = i
            }
            reorder(data, newIndices, false)
        } else {
            val newData = data.toMutableList()
            for (i in data.indices) {
                val index = indices[i]
                newData[i] = data[index]
            }
            newData
        }
    }

    fun <T : Comparable<T>> sortIndices(data: List<T>): List<Int> {
        return data.mapIndexed { index, value ->
            index to value
        }.sortedBy { it.second }.map { it.first }
    }

    fun <T : Comparable<T>> sortIndicesDescending(data: List<T>): List<Int> {
        return data.mapIndexed { index, value ->
            index to value
        }.sortedByDescending { it.second }.map { it.first }
    }
}
