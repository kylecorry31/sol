package com.kylecorry.sol.math.classifiers

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.algebra.*
import com.kylecorry.sol.math.batch
import com.kylecorry.sol.math.statistics.Statistics
import com.kylecorry.sol.math.sumOfFloat
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

class NeuralNetwork(
    private val layers: List<Layer>,
    weights: List<LayerWeights>? = null
) : IClassifier {

    init {
        layers.zipWithNext().forEach {
            if (it.first.outputSize != it.second.inputSize) {
                throw Exception("Layer with output of ${it.first.outputSize} can't connect to layer with input of ${it.second.inputSize}")
            }
        }
        weights?.let { load(it) }
    }

    fun predict(x: List<Float>): List<Float> {
        val input = columnMatrix(values = x.toFloatArray())
        return predict(input).transpose()[0].toList()
    }

    override fun classify(x: List<Float>): List<Float> {
        val prediction = predict(x)
        return if (layers.last().isClassifier) {
            prediction
        } else {
            Statistics.softmax(prediction)
        }
    }

    private fun predict(x: Matrix): Matrix {
        var input = x
        layers.forEach { input = it.activate(input) }
        return input
    }

    fun fitClasses(
        input: List<List<Float>>,
        output: List<Int>,
        epochs: Int = 100,
        learningRate: Float = 0.1f,
        regularization: Float = 0f,
        batchSize: Int = input.size,
        onEpochCompleteFn: (error: Float, epoch: Int) -> Unit = { _, _ -> }
    ): Float {
        val x = input.map { rowMatrix(values = it.toFloatArray()) }
        val y = output.map {
            rowMatrix(
                values = SolMath.oneHot(it, layers.last().outputSize, 1f, 0f).toFloatArray()
            )
        }
        return fit(x, y, epochs, learningRate, regularization, batchSize, onEpochCompleteFn)
    }

    fun fit(
        input: List<Matrix>,
        output: List<Matrix>,
        epochs: Int = 100,
        learningRate: Float = 0.1f,
        regularization: Float = 0f,
        batchSize: Int = input.size,
        onEpochCompleteFn: (error: Float, epoch: Int) -> Unit = { _, _ -> }
    ): Float {
        if (input.size != output.size) {
            throw IllegalArgumentException("Input and output have the same number of elements")
        }

        if (input.isEmpty()) {
            return 0f
        }

        val inputSize = input.first().rows() to input.first().columns()
        val outputSize = output.first().rows() to output.first().columns()

        if (inputSize.second != layers.first().inputSize) {
            throw IllegalArgumentException("Input of dimension ${inputSize.second} can't be fed to network with input dimension of ${layers.first().inputSize}")
        }

        if (outputSize.second != layers.last().outputSize) {
            throw IllegalArgumentException("Output of dimension ${inputSize.second} can't be produced by network with output dimension of ${layers.last().outputSize}")
        }

        if (input.any { it.rows() != inputSize.first || it.columns() != inputSize.second }) {
            throw IllegalArgumentException("All input matrices must have the same dimensions")
        }

        if (output.any { it.rows() != outputSize.first || it.columns() != outputSize.second }) {
            throw IllegalArgumentException("All output matrices must have the same dimensions")
        }

        var totalError = 0f
        val randomized = input.zip(output).shuffled()
        val batches = randomized.batch(batchSize)
        for (epoch in 0 until epochs) {
            for (n in batches.indices) {
                totalError = 0f
                val batch = batches[n].unzip()
                val inputRow = batch.first.map { it[0] }.toTypedArray().transpose()
                val outputRow = batch.second.map { it[0] }.toTypedArray().transpose()
                val predicted = predict(inputRow)
                val samples = inputRow.columns()
                val ones = columnMatrix(values = FloatArray(samples) { 1f })

                val previousLayerOutputs =
                    listOf(inputRow.transpose()) + layers.map { it.output.transpose() }
                        .take(layers.size - 1)

                var previousDelta: Matrix = createMatrix(0, 0, 0f)
                for (l in layers.indices.reversed()) {
                    previousDelta = if (l == layers.lastIndex) {
                        outputRow.subtract(predicted)
                    } else {
                        layers[l + 1].weights.transpose().dot(previousDelta)
                    }.multiply(-1f).multiply(layers[l].derivative(layers[l].input))
                    val change = previousDelta.dot(previousLayerOutputs[l])
                        .add(layers[l].weights.multiply(regularization))
                    layers[l].weights =
                        layers[l].weights.subtract(change.multiply(learningRate / samples))
                    val db = previousDelta.dot(ones).multiply(learningRate / samples)
                    layers[l].bias = layers[l].bias.subtract(db)
                }

                totalError += squaredError(inputRow, outputRow, regularization)
            }
            onEpochCompleteFn(totalError, epoch)
        }
        return totalError
    }

    private fun squaredError(x: Matrix, y: Matrix, regularization: Float): Float {
        val y_ = predict(x)
        val sumSquareWeights = layers.sumOfFloat { it.weights.mapped { it.pow(2) }.sum() }
        return 0.5f * y_.subtract(y).mapped { it.pow(2) }
            .sum() / layers.first().inputSize + regularization / 2 * sumSquareWeights
    }

    fun load(weights: List<LayerWeights>) {
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

    fun dump(): List<LayerWeights> {
        return layers.map { LayerWeights(it.weights, it.bias) }
    }


    class LayerWeights(val weights: Matrix, val bias: Matrix) {
        fun format(): String {
            return weights.zip(bias)
                .joinToString("\n") { it.first.joinToString(",") + "," + it.second.joinToString(",") }
        }

        companion object {
            fun parse(data: String): LayerWeights {
                val parsed = data.split("\n").map {
                    val row = it.split(",").mapNotNull { it.toFloatOrNull() }
                    row.take(row.size - 1).toTypedArray() to row.takeLast(1).toTypedArray()
                }
                val weights = parsed.map { it.first }.toTypedArray()
                val bias = parsed.map { it.second }.toTypedArray()
                return LayerWeights(weights, bias)
            }
        }
    }

    class Layer(
        val inputSize: Int,
        val outputSize: Int,
        private val activationFn: (Matrix) -> Matrix,
        private val activationDerivativeFn: (Matrix) -> Matrix,
        val isClassifier: Boolean = false
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
            this.output = activationFn(this.input)
            return this.output
        }

        internal fun derivative(input: Matrix): Matrix {
            return activationDerivativeFn(input)
        }

        private fun randomMatrix(rows: Int, columns: Int): Matrix {
            return createMatrix(rows, columns) { _, _ -> Math.random().toFloat() }
        }

        companion object {
            fun functionalLayer(
                input: Int,
                output: Int,
                activationFn: (Float) -> Float,
                activationDerivativeFn: (Float) -> Float,
                isClassifier: Boolean = false
            ): Layer {
                return Layer(
                    input,
                    output,
                    { it.mapped(activationFn) },
                    { it.mapped(activationDerivativeFn) },
                    isClassifier
                )
            }

            fun softmax(input: Int, output: Int): Layer {
                val grad = { x: FloatArray ->
                    val softmax = Statistics.softmax(x.toList()).toFloatArray()
                    val diag = diagonalMatrix(values = softmax)
                    val col = arrayOf(softmax.toTypedArray())
                    // This should be diag - col*colT but for some reason that cause error to increase
                    val grad = diag.add(col.dot(col.transpose()))
                    grad.sumColumns()[0].toFloatArray()
                }
                return Layer(
                    input,
                    output,
                    { it.mapColumns { Statistics.softmax(it.toList()).toFloatArray() } },
                    { it.mapColumns(grad) },
                    isClassifier = true
                )
            }

            fun binary(input: Int, output: Int): Layer {
                return functionalLayer(
                    input,
                    output,
                    { if (it > 0) 1f else 0f },
                    { 0f })
            }

            fun linear(input: Int, output: Int): Layer {
                return functionalLayer(
                    input,
                    output,
                    { it },
                    { 1f })
            }

            fun leakyRelu(input: Int, output: Int): Layer {
                return functionalLayer(
                    input,
                    output,
                    { if (it > 0) it else 0.01f * it },
                    { if (it > 0) 1f else 0.01f })
            }

            fun relu(input: Int, output: Int): Layer {
                return functionalLayer(
                    input,
                    output,
                    { if (it > 0) it else 0f },
                    { if (it > 0) 1f else 0f })
            }

            fun sigmoid(input: Int, output: Int): Layer {
                return functionalLayer(input,
                    output,
                    { 1f / (1 + exp(-it)) },
                    {
                        val a = 1f / (1 + exp(-it))
                        a * (1 - a)
                    })
            }

            fun softplus(input: Int, output: Int): Layer {
                return functionalLayer(input,
                    output,
                    { ln(1f + exp(it)) },
                    { 1f / (1 + exp(-it)) })
            }

            fun tanh(input: Int, output: Int): Layer {
                return functionalLayer(input,
                    output,
                    { kotlin.math.tanh(it) },
                    { 1 - kotlin.math.tanh(it).pow(2) })
            }
        }

    }

}