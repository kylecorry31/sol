package com.kylecorry.sol.math.classifiers

import com.kylecorry.sol.math.algebra.Matrix
import com.kylecorry.sol.math.statistics.Statistics
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.sin

internal class NeuralNetworkTest {

    @Test
    fun canLoadWeights() {
        val network = NeuralNetwork(
            listOf(
                NeuralNetwork.Layer.linear(3, 5),
                NeuralNetwork.Layer.softmax(5, 2)
            )
        )

        val weights = """0.39372277,0.6588374,0.11174718,0.1
0.6232503,0.12853253,0.90093297,0.1
0.5397353,0.8191797,0.54229444,0.1
0.55850136,0.36370513,0.04915447,0.1
0.5024289,0.63764894,0.3045396,0.1

0.91725606,0.44583938,0.2289399,0.43482378,0.8192703,0.1
0.81663775,0.7329151,0.3737688,0.103036165,0.58352137,0.1"""

        network.load(weights.split("\n\n").map { NeuralNetwork.LayerWeights.parse(it) })


        val prediction = network.predict(listOf(1f, 2f, 3f))

        assertEquals(2, prediction.size)
        assertEquals(0.44026583f, prediction[0], 0.0001f)
        assertEquals(0.55973417f, prediction[1], 0.0001f)
    }

    @Test
    fun canDumpWeights() {
        val network = NeuralNetwork(
            listOf(
                NeuralNetwork.Layer.linear(3, 5),
                NeuralNetwork.Layer.softmax(5, 2)
            )
        )

        val weights = """0.39372277,0.6588374,0.11174718,0.1
0.6232503,0.12853253,0.90093297,0.1
0.5397353,0.8191797,0.54229444,0.1
0.55850136,0.36370513,0.04915447,0.1
0.5024289,0.63764894,0.3045396,0.1

0.91725606,0.44583938,0.2289399,0.43482378,0.8192703,0.1
0.81663775,0.7329151,0.3737688,0.103036165,0.58352137,0.1"""

        network.load(weights.split("\n\n").map { NeuralNetwork.LayerWeights.parse(it) })

        val dumped = network.dump()

        assertEquals(weights, dumped.joinToString("\n\n") { it.format() })
    }

    @Test
    fun train() {
        val network = NeuralNetwork(
            listOf(
                NeuralNetwork.Layer.leakyRelu(2, 5),
                NeuralNetwork.Layer.sigmoid(5, 5),
                NeuralNetwork.Layer.softmax(5, 2)
            )
        )

        val x = (0..100).map { Matrix.row(it / 100f, it / 50f) }

        val y = (0..100).map {
            val res = sin(it / 100f) * sin(it / 50f)
            val classification = if (res < 0f) 0f else 1f
            Matrix.row(classification, 1 - classification)
        }

        val before = (0..100).map {
            val res = sin(it / 100f) * sin(it / 50f)
            val classification = if (res < 0f) 0f else 1f
            val expected = listOf(classification, 1 - classification)
            Statistics.rmse(network.predict(listOf(it / 100f, it / 50f)), expected)
        }.average()

        val error = network.fit(
            x,
            y,
            epochs = 2000,
            learningRate = 0.05f
        )

        val after = (0..100).map {
            val res = sin(it / 100f) * sin(it / 50f)
            val classification = if (res < 0f) 0f else 1f
            val expected = listOf(classification, 1 - classification)
            Statistics.rmse(network.predict(listOf(it / 100f, it / 50f)), expected)
        }.average()

        assertTrue(before > after)
        assertTrue(error < 0.01f)
    }
}