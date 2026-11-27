package com.kylecorry.sol.math.classifiers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class BinaryLogisticClassifierTest {

    @ParameterizedTest
    @CsvSource(
        "3, 0.5, 2.5, true",
        "3, 0.9, 0.3028, true",
        "-1, 0.5, -1.5, false",
        "1, 0.62, 0.0105, true"
    )
    fun comparesTheLogOddsToTheThreshold(
        x: Float,
        threshold: Float,
        expectedMargin: Float,
        expected: Boolean
    ) {
        val classifier = BinaryLogisticClassifier(
            floatArrayOf(1f), floatArrayOf(2f), 0.5f, floatArrayOf(2f), threshold
        )

        assertEquals(expectedMargin, classifier.margin(listOf(x)), 0.001f)
        assertEquals(expected, classifier.predict(listOf(x)))
    }

    @ParameterizedTest
    @CsvSource(
        "3, 0.5, 0.924142",
        "3, 0.9, 0.924142",
        "-1, 0.5, 0.182426"
    )
    fun classifiesTheProbabilityIgnoringTheThreshold(x: Float, threshold: Float, expected: Float) {
        val classifier = BinaryLogisticClassifier(
            floatArrayOf(1f), floatArrayOf(2f), 0.5f, floatArrayOf(2f), threshold
        )

        val probabilities = classifier.classify(listOf(x))

        assertEquals(expected, probabilities[1], 0.0001f)
        assertEquals(1 - expected, probabilities[0], 0.0001f)
    }

    @Test
    fun rejectsMismatchedSizes() {
        assertThrows(IllegalArgumentException::class.java) {
            BinaryLogisticClassifier(floatArrayOf(1f, 2f), floatArrayOf(2f), 0.5f, floatArrayOf(2f))
        }
        val classifier = BinaryLogisticClassifier(floatArrayOf(1f), floatArrayOf(2f), 0.5f, floatArrayOf(2f))
        assertThrows(IllegalArgumentException::class.java) { classifier.margin(listOf(1f, 2f)) }
    }
}
