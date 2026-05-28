package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.algebra.Matrix
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.roundToInt

internal class HoughTransformTest {

    @Test
    fun voteCreatesParameterSpace() {
        val parameterSpace = HoughTransform.vote(
            gradients = gradients(
                arrayOf(
                    floatArrayOf(0f, 0f, 0f),
                    floatArrayOf(0f, 0f, 0f),
                    floatArrayOf(0f, 0f, 0f),
                    floatArrayOf(0f, 0f, 0f)
                )
            ),
            threshold = 1f,
            thetaBinCount = 3,
            startThetaDegrees = 0f,
            endThetaDegrees = 180f
        )

        assertEquals(11, parameterSpace.rhoBinCount)
        assertEquals(3, parameterSpace.thetaBinCount)
        assertEquals(5f, parameterSpace.maxRho, 0.0001f)
        assertEquals(0f, parameterSpace.thetaValues[0], 0.0001f)
        assertEquals(Math.PI.toFloat() / 2f, parameterSpace.thetaValues[1], 0.0001f)
        assertEquals(Math.PI.toFloat(), parameterSpace.thetaValues[2], 0.0001f)
        assertTrue(parameterSpace.accumulatorBins.all { it == 0f })
    }

    @Test
    fun voteUsesMidpointThetaWhenThereIsOneThetaBin() {
        val parameterSpace = HoughTransform.vote(
            gradients = gradients(
                arrayOf(
                    floatArrayOf(0f, 0f, 0f),
                    floatArrayOf(0f, 0f, 0f),
                    floatArrayOf(0f, 0f, 0f),
                    floatArrayOf(0f, 0f, 0f)
                )
            ),
            threshold = 1f,
            thetaBinCount = 1,
            startThetaDegrees = 30f,
            endThetaDegrees = 90f
        )

        assertEquals(Math.PI.toFloat() / 3f, parameterSpace.thetaValues[0], 0.0001f)
    }

    @Test
    fun voteCastsPointWeightIntoEachThetaBin() {
        val parameterSpace = HoughTransform.vote(
            gradients = gradients(
                arrayOf(
                    floatArrayOf(0f, 0f, 0f),
                    floatArrayOf(0f, 0f, 0f),
                    floatArrayOf(0f, 3f, 0f),
                    floatArrayOf(0f, 0f, 0f)
                )
            ),
            threshold = 1f,
            thetaBinCount = 2,
            startThetaDegrees = 0f,
            endThetaDegrees = 90f
        )

        assertEquals(3f, parameterSpace.score(thetaBinIndex = 0, rhoBinIndex = rhoToBinIndex(1f, 5f, 11)), 0.0001f)
        assertEquals(3f, parameterSpace.score(thetaBinIndex = 1, rhoBinIndex = rhoToBinIndex(2f, 5f, 11)), 0.0001f)
        assertEquals(6f, parameterSpace.accumulatorBins.sum(), 0.0001f)
    }

    @Test
    fun voteAccumulatesWeightsInSameBin() {
        val parameterSpace = HoughTransform.vote(
            gradients = gradients(
                arrayOf(
                    floatArrayOf(0f, 2f, 0f),
                    floatArrayOf(0f, 0f, 0f),
                    floatArrayOf(0f, 0f, 0f),
                    floatArrayOf(0f, 4f, 0f)
                )
            ),
            threshold = 1f,
            thetaBinCount = 1,
            startThetaDegrees = 0f,
            endThetaDegrees = 0f
        )

        assertEquals(6f, parameterSpace.score(thetaBinIndex = 0, rhoBinIndex = rhoToBinIndex(1f, 5f, 11)), 0.0001f)
        assertEquals(6f, parameterSpace.accumulatorBins.sum(), 0.0001f)
    }

    @Test
    fun voteIgnoresPointsBelowThreshold() {
        val parameterSpace = HoughTransform.vote(
            gradients = gradients(
                arrayOf(
                    floatArrayOf(0f, 0f, 0f),
                    floatArrayOf(0f, 0.5f, 0f),
                    floatArrayOf(0f, 2f, 0f),
                    floatArrayOf(0f, 0f, 0f)
                )
            ),
            threshold = 1f,
            thetaBinCount = 1,
            startThetaDegrees = 90f,
            endThetaDegrees = 90f
        )

        assertEquals(0f, parameterSpace.score(thetaBinIndex = 0, rhoBinIndex = rhoToBinIndex(1f, 5f, 11)), 0.0001f)
        assertEquals(2f, parameterSpace.score(thetaBinIndex = 0, rhoBinIndex = rhoToBinIndex(2f, 5f, 11)), 0.0001f)
        assertEquals(2f, parameterSpace.accumulatorBins.sum(), 0.0001f)
    }

    @Test
    fun voteRequiresPositiveThetaBinCount() {
        assertThrows(IllegalArgumentException::class.java) {
            HoughTransform.vote(
                gradients = gradients(arrayOf(floatArrayOf(0f))),
                threshold = 1f,
                thetaBinCount = 0,
                startThetaDegrees = 0f,
                endThetaDegrees = 180f
            )
        }
    }

    private fun rhoToBinIndex(rho: Float, maxRho: Float, rhoBinCount: Int): Int {
        return (((rho + maxRho) / (2f * maxRho)) * (rhoBinCount - 1))
            .roundToInt()
            .coerceIn(0, rhoBinCount - 1)
    }

    private fun gradients(magnitude: Array<FloatArray>): Gradients {
        val matrix = Matrix.create(magnitude)
        return Gradients(
            x = Matrix.zeros(matrix.rows(), matrix.columns()),
            y = Matrix.zeros(matrix.rows(), matrix.columns()),
            magnitude = matrix
        )
    }
}
