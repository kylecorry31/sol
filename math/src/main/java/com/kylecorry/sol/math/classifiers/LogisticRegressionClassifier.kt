package com.kylecorry.sol.math.classifiers

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.algebra.*
import com.kylecorry.sol.math.statistics.Statistics
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.log
import kotlin.math.min

/**
 * A logistic regression classifier
 * @param weights an RxC matrix where R is the length of X and C is the number of classes
 */
class LogisticRegressionClassifier(
    private val input: Int,
    private val output: Int,
    private var weights: Matrix = createMatrix(input, output) { _, _ -> Math.random().toFloat() }
) : IClassifier {

    override fun classify(x: List<Float>): List<Float> {
        val input = createMatrix(1, x.size) { _, c -> x[c] }
        return classify(input)[0].toList()
    }

    private fun classify(x: Matrix): Matrix {
        val z = x.dot(weights)
        return z.mapColumns { Statistics.softmax(it.toList()).toFloatArray() }
    }

    fun fitClasses(
        input: List<List<Float>>,
        output: List<Int>,
        epochs: Int = 100,
        learningRate: Float = 0.1f,
        batchSize: Int = input.size,
        onEpochCompleteFn: (error: Float, epoch: Int) -> Unit = { _, _ -> }
    ): Float {
        val x = input.map { rowMatrix(values = it.toFloatArray()) }
        val y = output.map {
            rowMatrix(
                values = SolMath.oneHot(it, this.output, 1f, 0f).toFloatArray()
            )
        }
        return fit(x, y, epochs, learningRate, batchSize, onEpochCompleteFn)
    }

    fun fit(
        input: List<Matrix>,
        output: List<Matrix>,
        epochs: Int = 100,
        learningRate: Float = 0.1f,
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

        if (inputSize.second != this.input) {
            throw IllegalArgumentException("Input of dimension ${inputSize.second} can't be fed to network with input dimension of ${this.input}")
        }

        if (outputSize.second != this.output) {
            throw IllegalArgumentException("Output of dimension ${inputSize.second} can't be produced by network with output dimension of ${this.output}")
        }

        if (input.any { it.rows() != inputSize.first || it.columns() != inputSize.second }) {
            throw IllegalArgumentException("All input matrices must have the same dimensions")
        }

        if (output.any { it.rows() != outputSize.first || it.columns() != outputSize.second }) {
            throw IllegalArgumentException("All output matrices must have the same dimensions")
        }

        var totalError = 0f
        val randomized = randomize(input, output)
        val batches = ceil(input.size / batchSize.toFloat()).toInt()
        for (epoch in 0 until epochs) {
            for (n in 0 until batches) {
                totalError = 0f
                val batch = getBatch(randomized.first, randomized.second, batchSize, n * batchSize)
                val inputRow = batch.first
                val outputRow = batch.second
                val gradient = crossEntropyGradient(inputRow, outputRow)
                weights = weights.subtract(gradient.multiply(learningRate))
                totalError += crossEntropy(inputRow, outputRow)
            }
            onEpochCompleteFn(totalError, epoch)
        }

        return totalError
    }

    fun dump(): Matrix {
        return weights.mapped { it }
    }

    fun load(weights: Matrix) {
        this.weights = weights
    }

    private fun crossEntropy(x: Matrix, y: Matrix): Float {
        val predictions = classify(x)
        return y.multiply(-1f).multiply(predictions.mapped { ln(it) }).sum() / y.rows()
    }

    private fun crossEntropyGradient(x: Matrix, y: Matrix): Matrix {
        val n = y.rows()
        val predictions = classify(x).subtract(y)
        return x.transpose().dot(predictions).divide(n.toFloat())
    }

    private fun randomize(x: List<Matrix>, y: List<Matrix>): Pair<List<Matrix>, List<Matrix>> {
        return x.zip(y).shuffled().unzip()
    }

    private fun getBatch(
        x: List<Matrix>,
        y: List<Matrix>,
        batchSize: Int,
        start: Int
    ): Pair<Matrix, Matrix> {
        val end = min(start + batchSize, x.size) - 1
        val range = IntRange(start, end)

        val batch = x.zip(y).slice(range).unzip()

        val inputMatrix = batch.first.map { it[0] }.toTypedArray().transpose()
        val outputMatrix = batch.second.map { it[0] }.toTypedArray().transpose()
        return inputMatrix to outputMatrix
    }

    companion object {
        fun withWeights(weights: Matrix): LogisticRegressionClassifier {
            return LogisticRegressionClassifier(weights.rows(), weights.columns(), weights)
        }
    }
}