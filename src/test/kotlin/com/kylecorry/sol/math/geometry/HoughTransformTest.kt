package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.roundToInt

internal class HoughTransformTest {

    @Test
    fun voteCreatesParameterSpace() {
        val parameterSpace = HoughTransform.vote(
            emptyList(),
            imageWidth = 3,
            imageHeight = 4,
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
            emptyList(),
            imageWidth = 3,
            imageHeight = 4,
            thetaBinCount = 1,
            startThetaDegrees = 30f,
            endThetaDegrees = 90f
        )

        assertEquals(Math.PI.toFloat() / 3f, parameterSpace.thetaValues[0], 0.0001f)
    }

    @Test
    fun voteCastsPointWeightIntoEachThetaBin() {
        val parameterSpace = HoughTransform.vote(
            listOf(WeightedPoint(Vector2(1f, 2f), 3f)),
            imageWidth = 3,
            imageHeight = 4,
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
            listOf(
                WeightedPoint(Vector2(1f, 0f), 2f),
                WeightedPoint(Vector2(1f, 3f), 4f)
            ),
            imageWidth = 3,
            imageHeight = 4,
            thetaBinCount = 1,
            startThetaDegrees = 0f,
            endThetaDegrees = 0f
        )

        assertEquals(6f, parameterSpace.score(thetaBinIndex = 0, rhoBinIndex = rhoToBinIndex(1f, 5f, 11)), 0.0001f)
        assertEquals(6f, parameterSpace.accumulatorBins.sum(), 0.0001f)
    }

    @Test
    fun voteRequiresPositiveInputs() {
        assertThrows(IllegalArgumentException::class.java) {
            HoughTransform.vote(emptyList(), 1, 1, 0, 0f, 180f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            HoughTransform.vote(emptyList(), 0, 1, 1, 0f, 180f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            HoughTransform.vote(emptyList(), 1, 0, 1, 0f, 180f)
        }
    }

    private fun rhoToBinIndex(rho: Float, maxRho: Float, rhoBinCount: Int): Int {
        return (((rho + maxRho) / (2f * maxRho)) * (rhoBinCount - 1))
            .roundToInt()
            .coerceIn(0, rhoBinCount - 1)
    }
}
