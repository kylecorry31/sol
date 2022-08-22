package com.kylecorry.sol.math.classifiers.neural_network

import com.kylecorry.sol.math.algebra.*
import com.kylecorry.sol.math.sumOfFloat
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log
import kotlin.math.pow

class NeuralNetwork(private val layers: List<NeuralNetworkLayer>) {
    fun predict(x: List<Float>): List<Float> {
        var input = columnMatrix(values = x.toFloatArray())
        layers.forEach { input = it.activate(input) }
        return input.transpose()[0].toList()
    }

    fun fit(
        input: List<Matrix>,
        output: List<Matrix>,
        epochs: Int = 100,
        learningRate: Float = 0.1f,
        regularization: Float = 0f,
        onEpochCompleteFn: (error: Float, epoch: Int) -> Unit = { _, _ -> }
    ): Float {
        // TODO: Check input and output sizes
        // TODO: Update bias weights
        // TODO: Merge output and hidden layer code
        var totalError = 0f
        for (epoch in 0 until epochs) {
            totalError = 0f
            for (i in input.indices) {
                val inputRow = input[i]
                val outputRow = output[i].transpose()
                val predicted =
                    columnMatrix(values = predict(inputRow[0].toList()).toFloatArray())

                // Output layer
                val previousDelta = outputRow.subtract(predicted).multiply(-1f)
                    .multiply(layers.last().derivative(layers.last().input))
                val change = previousDelta.dot(layers[layers.size - 2].output.transpose())
                    .add(layers.last().weights.multiply(regularization))
                layers.last().weights =
                    layers.last().weights.subtract(change.multiply(learningRate))

                // Hidden layers
                for (l in (1..layers.size - 2).reversed()) {
                    val previousDelta = layers[l + 1].weights.transpose().dot(previousDelta)
                        .multiply(layers[l].output.transpose())
                    val change = previousDelta.dot(layers[l - 1].output.transpose())
                        .add(layers[l].weights.multiply(regularization))
                    layers[l].weights = layers[l].weights.subtract(change.multiply(learningRate))
                }
                totalError += squaredError(inputRow, outputRow, regularization)
            }
            onEpochCompleteFn(totalError, epoch)
        }
        return totalError
    }

    private fun squaredError(x: Matrix, y: Matrix, regularization: Float): Float {
        val y_ = columnMatrix(values = predict(x[0].toList()).toFloatArray())
        val sumSquareWeights = layers.sumOfFloat { it.weights.mapped { it.pow(2) }.sum() }
        return 0.5f * y_.subtract(y).mapped { it.pow(2) }
            .sum() / layers.first().inputSize + regularization / 2 * sumSquareWeights
    }

    fun load(weights: List<NeuralNetworkLayerWeights>) {
        if (weights.size != layers.size) {
            throw Exception("Weights and bias lists must be same size as layers")
        }
        layers.forEachIndexed { index, layer ->
            if (layer.weights.columns() != weights[index].weights.columns() || layer.weights.rows() != weights[index].weights.rows()) {
                throw Exception("Weight matrix for layer $index must be the same size")
            }
            if (layer.bias.columns() != weights[index].bias.columns() || layer.bias.rows() != weights[index].bias.rows()) {
                throw Exception("Bias matrix for layer $index must be the same size")
            }
            layer.weights = weights[index].weights
            layer.bias = weights[index].bias
        }
    }

    fun dump(): List<NeuralNetworkLayerWeights> {
        return layers.map { NeuralNetworkLayerWeights(it.weights, it.bias) }
    }
}

class NeuralNetworkLayerWeights(val weights: Matrix, val bias: Matrix) {
    fun format(): String {
        return weights.zip(bias)
            .joinToString("\n") { it.first.joinToString(",") + "," + it.second.joinToString(",") }
    }

    companion object {
        fun parse(data: String): NeuralNetworkLayerWeights {
            val parsed = data.split("\n").map {
                val row = it.split(",").mapNotNull { it.toFloatOrNull() }
                row.take(row.size - 1).toTypedArray() to row.takeLast(1).toTypedArray()
            }
            val weights = parsed.map { it.first }.toTypedArray()
            val bias = parsed.map { it.second }.toTypedArray()
            return NeuralNetworkLayerWeights(weights, bias)
        }
    }
}

class NeuralNetworkLayer(
    val inputSize: Int,
    val outputSize: Int,
    private val activationFn: (Float) -> Float,
    private val activationDerivativeFn: (Float) -> Float,
    private val isSoftmax: Boolean = false
) {

    internal var weights: Matrix
    internal var bias: Matrix
    internal var output: Matrix
    internal var input: Matrix

    init {
        weights = randomMatrix(outputSize, inputSize)
        bias = createMatrix(outputSize, 1, 0.1f)
        output = createMatrix(outputSize, 1, 0f)
        input = createMatrix(inputSize, 1, 0f)
    }

    internal fun activate(input: Matrix): Matrix {
        this.input = weights.dot(input).add(bias)
        this.output = if (isSoftmax) {
            this.input.mapped(activationFn).let {
                val sum = it.sum()
                if (sum != 0f) it.multiply(1 / sum) else it
            }
        } else {
            this.input.mapped(activationFn)
        }
        return this.output
    }

    internal fun derivative(input: Matrix): Matrix {
        return if (isSoftmax) {
            input.mapped(activationDerivativeFn).let {
                val sum = this.input.sum()
                if (sum != 0f) it.multiply(1 / sum) else it
            }.subtract(input)
        } else {
            input.mapped(activationDerivativeFn)
        }
    }

    private fun randomMatrix(rows: Int, columns: Int): Matrix {
        return createMatrix(rows, columns) { _, _ -> Math.random().toFloat() }
    }


    companion object {
        fun softmax(input: Int, output: Int): NeuralNetworkLayer {
            return NeuralNetworkLayer(
                input,
                output,
                { exp(it) },
                { exp(it) },
                isSoftmax = true
            )
        }

        fun binary(input: Int, output: Int): NeuralNetworkLayer {
            return NeuralNetworkLayer(
                input,
                output,
                { if (it > 0) 1f else 0f },
                { 0f })
        }

        fun linear(input: Int, output: Int): NeuralNetworkLayer {
            return NeuralNetworkLayer(
                input,
                output,
                { it },
                { 1f })
        }

        fun leakyRelu(input: Int, output: Int): NeuralNetworkLayer {
            return NeuralNetworkLayer(
                input,
                output,
                { if (it > 0) it else 0.01f * it },
                { if (it > 0) 1f else 0.01f })
        }

        fun relu(input: Int, output: Int): NeuralNetworkLayer {
            return NeuralNetworkLayer(
                input,
                output,
                { if (it > 0) it else 0f },
                { if (it > 0) 1f else 0f })
        }

        fun sigmoid(input: Int, output: Int): NeuralNetworkLayer {
            return NeuralNetworkLayer(input,
                output,
                { 1f / (1 + exp(-it)) },
                {
                    val a = 1f / (1 + exp(-it))
                    a * (1 - a)
                })
        }

        fun softplus(input: Int, output: Int): NeuralNetworkLayer {
            return NeuralNetworkLayer(input,
                output,
                { ln(1f + exp(it)) },
                { 1f / (1 + exp(-it)) })
        }

        fun tanh(input: Int, output: Int): NeuralNetworkLayer {
            return NeuralNetworkLayer(input,
                output,
                { kotlin.math.tanh(it) },
                { 1 - kotlin.math.tanh(it).pow(2) })
        }
    }

}