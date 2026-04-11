package com.kylecorry.sol.math.classifiers

import com.kylecorry.sol.math.algebra.Matrix
import com.kylecorry.sol.math.arithmetic.Arithmetic.square
import com.kylecorry.sol.math.statistics.Statistics
import kotlin.math.sqrt

class KNNClassifier(
    private val k: Int,
    private val input: Matrix,
    private val labels: Array<Array<Int>>
) :
    IClassifier {

    override fun classify(x: List<Float>): List<Float> {
        val xArr = x.toTypedArray()

        val tempNeighbors = mutableListOf<Pair<Array<Int>, Float>>()
        for (row in 0 until input.rows()) {
            tempNeighbors.add(labels[row] to distance(xArr, input.getRow(row).toTypedArray()))
        }
        val neighbors = tempNeighbors.sortedBy { it.second }.take(k)

        val inverseSum =
            neighbors.sumOf { 1 / it.second.toDouble().coerceAtLeast(0.000001) }
        val weightedNeighbors =
            neighbors.map { it.first to (1 / it.second.coerceAtLeast(0.000001f)) / inverseSum }

        val sumLabels = MutableList(labels[0].size) { 0f }
        for (neighbor in weightedNeighbors) {
            for (i in neighbor.first.indices) {
                sumLabels[i] += neighbor.first[i].toFloat() * neighbor.second.toFloat()
            }
        }

        return Statistics.probability(sumLabels)
    }

    private fun distance(p1: Array<Float>, p2: Array<Float>): Float {
        var sum = 0f
        for (i in p1.indices) {
            sum += square(p1[i] - p2[i])
        }
        return sqrt(sum)
    }
}