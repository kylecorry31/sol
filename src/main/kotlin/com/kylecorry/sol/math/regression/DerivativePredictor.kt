package com.kylecorry.sol.math.regression

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.calculus.Calculus

class DerivativePredictor(
    private val samples: List<Vector2>,
    private val order: Int = 2,
    private val configs: Map<Int, DerivativePredictorConfig> = emptyMap()
) {

    private val cachedSamples by lazy {
        withSmoothing(0, samples.sortedBy { it.x }).toMutableList()
    }

    private fun withSmoothing(order: Int, values: List<Vector2>): List<Vector2> {
        val smoothFn = configs[order]?.smoothFunction ?: return values
        return smoothFn(values)
    }

    private fun calculateLastStep(): Float? {
        if (cachedSamples.size < 2) {
            return null
        }
        return cachedSamples[cachedSamples.size - 1].x - cachedSamples[cachedSamples.size - 2].x
    }

    fun predictNext(n: Int, step: Float? = null): List<Vector2> {
        val actualStep = step ?: calculateLastStep() ?: 1f
        val values = mutableListOf(cachedSamples)
        for (i in 1..order) {
            val lastValues = values.last()
            values.add(withSmoothing(i, Calculus.derivative(lastValues)).toMutableList())
        }
        val predictions = mutableListOf<Vector2>()
        for (stepIndex in 0 until n) {
            val coefs = values.map { it.last() }
            val nextCoefs = mutableListOf<Vector2>()
            for (i in coefs.indices) {
                var nextValue = coefs[i].y
                for (j in i + 1 until coefs.size) {
                    val factorial = Arithmetic.factorial(j - i)
                    if (factorial != 0L) {
                        nextValue += coefs[j].y * (1f / factorial) * SolMath.power(
                            actualStep,
                            (j - i)
                        )
                    }
                }
                nextCoefs.add(Vector2(coefs[i].x + actualStep, nextValue))
            }
            // Apply damping and limits
            configs.forEach { (index, config) ->
                if (index >= nextCoefs.size) return@forEach

                val value = nextCoefs[index]
                var y = value.y

                config.dampingFactor?.let {
                    y *= it
                }

                config.limit?.let {
                    y = y.coerceIn(it.start, it.end)
                }

                nextCoefs[index] = Vector2(value.x, y)
            }
            predictions.add(nextCoefs[0])
            for (i in values.indices) {
                values[i].add(nextCoefs[i])
            }
        }
        return predictions
    }

    class DerivativePredictorConfig(
        val limit: Range<Float>? = null,
        val dampingFactor: Float? = null,
        val smoothFunction: ((List<Vector2>) -> List<Vector2>)? = null,
    )
}