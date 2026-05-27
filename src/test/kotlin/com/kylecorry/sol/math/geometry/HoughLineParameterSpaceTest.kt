package com.kylecorry.sol.math.geometry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.math.PI

internal class HoughLineParameterSpaceTest {

    @Test
    fun scoreReadsFromThetaMajorAccumulatorBins() {
        val parameterSpace = HoughLineParameterSpace(
            accumulatorBins = floatArrayOf(
                1f, 2f, 3f,
                4f, 5f, 6f
            ),
            rhoBinCount = 3,
            thetaBinCount = 2,
            maxRho = 10f,
            thetaValues = floatArrayOf(0f, PI.toFloat() / 2f)
        )

        assertEquals(1f, parameterSpace.score(thetaBinIndex = 0, rhoBinIndex = 0))
        assertEquals(3f, parameterSpace.score(thetaBinIndex = 0, rhoBinIndex = 2))
        assertEquals(4f, parameterSpace.score(thetaBinIndex = 1, rhoBinIndex = 0))
        assertEquals(6f, parameterSpace.score(thetaBinIndex = 1, rhoBinIndex = 2))
    }

    @Test
    fun getLineCandidateMapsRhoBinAcrossNegativeToPositiveMaxRho() {
        val parameterSpace = HoughLineParameterSpace(
            accumulatorBins = FloatArray(5),
            rhoBinCount = 5,
            thetaBinCount = 1,
            maxRho = 10f,
            thetaValues = floatArrayOf(PI.toFloat() / 4f)
        )

        val negativeMax = parameterSpace.getLineCandidate(thetaBinIndex = 0, rhoBinIndex = 0)
        val zero = parameterSpace.getLineCandidate(thetaBinIndex = 0, rhoBinIndex = 2)
        val positiveMax = parameterSpace.getLineCandidate(thetaBinIndex = 0, rhoBinIndex = 4)

        assertEquals(-10f, negativeMax.rho, 0.0001f)
        assertEquals(PI.toFloat() / 4f, negativeMax.thetaRadians, 0.0001f)
        assertEquals(0f, zero.rho, 0.0001f)
        assertEquals(10f, positiveMax.rho, 0.0001f)
    }

    @Test
    fun getLineCandidateUsesZeroRhoWhenThereIsOneRhoBin() {
        val parameterSpace = HoughLineParameterSpace(
            accumulatorBins = FloatArray(1),
            rhoBinCount = 1,
            thetaBinCount = 1,
            maxRho = 10f,
            thetaValues = floatArrayOf(PI.toFloat())
        )

        val line = parameterSpace.getLineCandidate(thetaBinIndex = 0, rhoBinIndex = 0)

        assertEquals(0f, line.rho, 0.0001f)
        assertEquals(PI.toFloat(), line.thetaRadians, 0.0001f)
    }

    @Test
    fun findStrongestLineCandidateReturnsHighestPositiveScoringLine() {
        val parameterSpace = HoughLineParameterSpace(
            accumulatorBins = floatArrayOf(
                0f, 2f, 1f,
                3f, 4f, 5f
            ),
            rhoBinCount = 3,
            thetaBinCount = 2,
            maxRho = 8f,
            thetaValues = floatArrayOf(0f, PI.toFloat() / 2f)
        )

        val line = requireNotNull(parameterSpace.findStrongestLineCandidate())

        assertEquals(8f, line.rho, 0.0001f)
        assertEquals(PI.toFloat() / 2f, line.thetaRadians, 0.0001f)
    }

    @Test
    fun findStrongestLineCandidateReturnsNullWhenThereAreNoPositiveScores() {
        val parameterSpace = HoughLineParameterSpace(
            accumulatorBins = floatArrayOf(0f, -1f, -2f),
            rhoBinCount = 3,
            thetaBinCount = 1,
            maxRho = 5f,
            thetaValues = floatArrayOf(0f)
        )

        assertNull(parameterSpace.findStrongestLineCandidate())
    }
}
