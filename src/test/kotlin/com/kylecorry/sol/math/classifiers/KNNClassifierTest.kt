package com.kylecorry.sol.math.classifiers

import com.kylecorry.sol.math.algebra.Matrix
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class KNNClassifierTest {

    @Test
    fun classify() {
        val knn = KNNClassifier(
            3, Matrix.create(arrayOf(
                floatArrayOf(22f),
                floatArrayOf(23f),
                floatArrayOf(21f),
                floatArrayOf(18f),
                floatArrayOf(19f),
                floatArrayOf(25f),
                floatArrayOf(27f),
                floatArrayOf(29f),
                floatArrayOf(31f),
                floatArrayOf(45f),
            )),
            arrayOf(
                intArrayOf(0, 1),
                intArrayOf(0, 1),
                intArrayOf(0, 1),
                intArrayOf(0, 1),
                intArrayOf(0, 1),
                intArrayOf(1, 0),
                intArrayOf(1, 0),
                intArrayOf(1, 0),
                intArrayOf(1, 0),
                intArrayOf(1, 0),
            )
        )

        val classification = knn.classify(listOf(33f))

        assertEquals(1f, classification[0], 0.0001f)
        assertEquals(0f, classification[1], 0.0001f)
    }
    
}
