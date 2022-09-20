package com.kylecorry.sol.math.classifiers

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class LogisticRegressionClassifierTest {

    @Test
    fun classify() {
        val weights = arrayOf(
            arrayOf(0.39778158f, 0.39860763f, -0.78607847f),
            arrayOf(0.83751194f, -0.35418033f, -0.56527192f),
            arrayOf(-1.16395315f, 0.15156467f, 1.27653258f),
            arrayOf(-0.4919542f, -0.25904599f, 0.76531921f),
        )

        val x = listOf(
            5.1f, 3.5f, 1.4f, 0.2f
        )

        val clf = LogisticRegressionClassifier.fromWeights(weights)
        val y = clf.classify(x)

        assertEquals(0.906505470f, y[0], 0.0001f)
        assertEquals(0.0928693387f, y[1], 0.0001f)
        assertEquals(0.000625191058f, y[2], 0.0001f)
    }
}