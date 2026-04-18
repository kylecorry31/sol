package com.kylecorry.sol.math.classifiers

import com.kylecorry.sol.math.algebra.Matrix
import com.kylecorry.sol.math.algebra.add
import com.kylecorry.sol.math.algebra.divide
import com.kylecorry.sol.math.algebra.dot
import com.kylecorry.sol.math.algebra.mapColumns
import com.kylecorry.sol.math.algebra.mapRows
import com.kylecorry.sol.math.algebra.mapped
import com.kylecorry.sol.math.algebra.multiply
import com.kylecorry.sol.math.algebra.subtract
import com.kylecorry.sol.math.algebra.sumColumns
import com.kylecorry.sol.math.algebra.sumRows
import com.kylecorry.sol.math.algebra.transpose
import com.kylecorry.sol.math.batch
import com.kylecorry.sol.math.lists.Lists
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
            require(it.first.outputSize == it.second.inputSize) {
                "Layer with output of ${it.first.outputSize} can't connect to layer with " +
                    "input of ${it.second.inputSize}"
            }
        }
        weights?.let { load(it) }
    }

    fun predict(x: List<Float>): List<Float> {
        val input = Matrix.column(values = x.toFloatArray())
        return predict(input).getColumn(0).toList()
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
        val x = input.map { Matrix.row(values = it.toFloatArray()) }
        val y = output.map {
            Matrix.row(
                values = Lists.oneHot(it, layers.last().outputSize, 1f, 0f).toFloatArray()
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
        require(input.size == output.size) { "Input and output have the same number of elements" }

        if (input.isEmpty()) {
            return 0f
        }

        val inputSize = input.first().rows() to input.first().columns()
        val outputSize = output.first().rows() to output.first().columns()

        require(inputSize.second == layers.first().inputSize) {
            "Input of dimension ${inputSize.second} can't be fed to network with input " +
                "dimension of ${layers.first().inputSize}"
        }

        require(outputSize.second == layers.last().outputSize) {
            "Output of dimension ${inputSize.second} can't be produced by network with " +
                "output dimension of ${layers.last().outputSize}"
        }

        require(input.none { it.rows() != inputSize.first || it.columns() != inputSize.second }) {
            "All input matrices must have the same dimensions"
        }

        require(output.none { it.rows() != outputSize.first || it.columns() != outputSize.second }) {
            "All output matrices must have the same dimensions"
        }

        var totalError = 0f
        val randomized = input.zip(output).shuffled()
        val batches = randomized.batch(batchSize)
        for (epoch in 0..<epochs) {
            for (n in batches.indices) {
                totalError = 0f
                val batch = batches[n].unzip()
                val inputRow = Matrix.create(batch.first.map { it.getRow(0) }.toTypedArray()).transpose()
                val outputRow =
                    Matrix.create(batch.second.map { it.getRow(0) }.toTypedArray()).transpose()
                val predicted = predict(inputRow)
                val samples = inputRow.columns()
                val ones = Matrix.column(values = FloatArray(samples) { 1f })

                val previousLayerOutputs =
                    listOf(inputRow.transpose()) + layers.map { it.output.transpose() }
                        .take(layers.size - 1)

                var previousDelta: Matrix = Matrix.zeros(0, 0)
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
        val yPrediction = predict(x)
        val sumSquareWeights = layers.sumOfFloat { it.weights.mapped { weight -> weight.pow(2) }.sum() }
        return 0.5f * yPrediction.subtract(y).mapped { it.pow(2) }
            .sum() / layers.first().inputSize + regularization / 2 * sumSquareWeights
    }

    fun load(weights: List<LayerWeights>) {
        require(weights.size == layers.size) {
            "Weights and bias lists must be same size as layers"
        }
        layers.forEachIndexed { index, layer ->
            require(
                layer.weights.columns() == weights[index].weights.columns() &&
                    layer.weights.rows() == weights[index].weights.rows()
            ) {
                "Weight matrix for layer $index must be the same size"
            }
            require(
                layer.bias.columns() == weights[index].bias.columns() &&
                    layer.bias.rows() == weights[index].bias.rows()
            ) {
                "Bias matrix for layer $index must be the same size"
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
            val builder = StringBuilder()
            for (row in 0..<weights.rows()) {
                val weightRow = weights.getRow(row)
                val biasRow = bias.getRow(row)
                builder.appendLine(weightRow.joinToString(",") + "," + biasRow.joinToString(","))
            }

            return builder.toString().trim()
        }

        companion object {
            fun parse(data: String): LayerWeights {
                val parsed = data.split("\n").map {
                    val row = it.split(",").mapNotNull { cell -> cell.toFloatOrNull() }
                    row.take(row.size - 1).toFloatArray() to row.takeLast(1).toFloatArray()
                }
                val weights = Matrix.create(parsed.map { it.first }.toTypedArray())
                val bias = Matrix.create(parsed.map { it.second }.toTypedArray())
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
            bias = Matrix.create(outputSize, 1, 0.1f)
            output = Matrix.zeros(outputSize, 1)
            input = Matrix.zeros(inputSize, 1)
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
            return Matrix.create(rows, columns) { _, _ -> Math.random().toFloat() }
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
                    val diag = Matrix.diagonal(values = softmax)
                    val col = Matrix.column(values = softmax)
                    // This should be diag - col*colT but for some reason that cause error to increase
                    val grad = diag.add(col.dot(col.transpose()))
                    grad.sumColumns().getRow(0)
                }
                return Layer(
                    input,
                    output,
                    { it.mapColumns { col -> Statistics.softmax(col.toList()).toFloatArray() } },
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
                return functionalLayer(
                    input,
                    output,
                    { 1f / (1 + exp(-it)) },
                    {
                        val a = 1f / (1 + exp(-it))
                        a * (1 - a)
                    })
            }

            fun softplus(input: Int, output: Int): Layer {
                return functionalLayer(
                    input,
                    output,
                    { ln(1f + exp(it)) },
                    { 1f / (1 + exp(-it)) })
            }

            fun tanh(input: Int, output: Int): Layer {
                return functionalLayer(
                    input,
                    output,
                    { kotlin.math.tanh(it) },
                    { 1 - kotlin.math.tanh(it).pow(2) })
            }
        }

    }

}
