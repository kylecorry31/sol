package com.kylecorry.sol.math.classifiers.neural_network

import com.kylecorry.sol.math.algebra.rowMatrix
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class NeuralNetworkTest {

    @Test
    fun predict() {
        val network = NeuralNetwork(
            listOf(
                NeuralNetworkLayer.linear(3, 5),
                NeuralNetworkLayer.softmax(5, 2)
            )
        )

        val weights = """0.39372277,0.6588374,0.11174718,0.1
0.6232503,0.12853253,0.90093297,0.1
0.5397353,0.8191797,0.54229444,0.1
0.55850136,0.36370513,0.04915447,0.1
0.5024289,0.63764894,0.3045396,0.1

0.91725606,0.44583938,0.2289399,0.43482378,0.8192703,0.1
0.81663775,0.7329151,0.3737688,0.103036165,0.58352137,0.1"""

        network.load(weights.split("\n\n").map { NeuralNetworkLayerWeights.parse(it) })


        val prediction = network.predict(listOf(1f, 2f, 3f))

        assertEquals(2, prediction.size)
        assertEquals(0.44026583f, prediction[0], 0.0001f)
        assertEquals(0.55973417f, prediction[1], 0.0001f)
    }

//    @Test
    fun train() {
        val network = NeuralNetwork(
            listOf(
                NeuralNetworkLayer.sigmoid(2, 5),
                NeuralNetworkLayer.softmax(5, 2)
            )
        )

       val x = listOf(
           rowMatrix(0f, 1f),
           rowMatrix(1f, 1f),
           rowMatrix(1f, 0f),
           rowMatrix(0f, 0f),
       )

        val y = listOf(
            rowMatrix(0f, 1f),
            rowMatrix(1f, 0f),
            rowMatrix(0f, 1f),
            rowMatrix(0f, 0f)
        )

        val error = network.fit(x, y, epochs = 2000){ error, epoch -> println("$epoch: $error")}
        println(error)
        val prediction = network.predict(listOf(1f, 0f))

        assertEquals(2, prediction.size)
        assertEquals(0f, prediction[0], 0.2f)
        assertEquals(1f, prediction[1], 0.2f)
    }
}