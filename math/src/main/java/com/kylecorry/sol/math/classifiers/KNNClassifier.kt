package com.kylecorry.sol.math.classifiers

import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.algebra.Matrix
import com.kylecorry.sol.math.statistics.StatisticsService
import kotlin.math.sqrt

class KNNClassifier(
    private val k: Int,
    private val input: Matrix,
    private val labels: Array<Array<Int>>
) :
    IClassifier {

    private val statistics = StatisticsService()

    override fun classify(x: List<Float>): List<Float> {
        val xArr = x.toTypedArray()
        val neighbors = input
            .mapIndexed { index, values -> labels[index] to distance(xArr, values) }
            .sortedBy { it.second }
            .take(k)

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

        return statistics.probability(sumLabels)
    }

    private fun distance(p1: Array<Float>, p2: Array<Float>): Float {
        var sum = 0f
        for (i in p1.indices) {
            sum += square(p1[i] - p2[i])
        }
        return sqrt(sum)
    }
}