package com.kylecorry.sol.math.classifiers

import com.kylecorry.sol.math.statistics.Statistics

class EnsembleClassifier(private val classifiers: List<IClassifier>) : IClassifier {

    override fun classify(x: List<Float>): List<Float> {
        if (classifiers.isEmpty()) {
            return emptyList()
        }

        val labels = classifiers.map { it.classify(x) }

        val sumLabels = MutableList(labels[0].size) { 0f }

        for (label in labels) {
            for (i in label.indices) {
                sumLabels[i] += label[i]
            }
        }

        return Statistics.probability(sumLabels)
    }
}