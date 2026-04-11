package com.kylecorry.sol.math.classifiers

import com.kylecorry.sol.math.algebra.Matrix
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class KNNClassifierTest {

    @Test
    fun classify() {
        val knn = KNNClassifier(
            3, Matrix.create(arrayOf(
                arrayOf(22f),
                arrayOf(23f),
                arrayOf(21f),
                arrayOf(18f),
                arrayOf(19f),
                arrayOf(25f),
                arrayOf(27f),
                arrayOf(29f),
                arrayOf(31f),
                arrayOf(45f),
            )),
            arrayOf(
                arrayOf(0, 1),
                arrayOf(0, 1),
                arrayOf(0, 1),
                arrayOf(0, 1),
                arrayOf(0, 1),
                arrayOf(1, 0),
                arrayOf(1, 0),
                arrayOf(1, 0),
                arrayOf(1, 0),
                arrayOf(1, 0),
            )
        )

        val classification = knn.classify(listOf(33f))

        assertEquals(1f, classification[0], 0.0001f)
        assertEquals(0f, classification[1], 0.0001f)
    }
    
}