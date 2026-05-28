package com.kylecorry.sol.math.geometry

import kotlin.math.max
import kotlin.math.min

class HoughLineParameterSpace(
    val accumulatorBins: FloatArray,
    val rhoBinCount: Int,
    val thetaBinCount: Int,
    val maxRho: Float,
    val thetaValues: FloatArray
) {
    fun score(thetaBinIndex: Int, rhoBinIndex: Int): Float {
        return accumulatorBins[thetaBinIndex * rhoBinCount + rhoBinIndex]
    }

    private fun getRho(rhoBinIndex: Int): Float {
        return if (rhoBinCount == 1) {
            0f
        } else {
            -maxRho + (2f * maxRho) * rhoBinIndex / (rhoBinCount - 1).toFloat()
        }
    }

    fun findStrongestLineCandidates(
        maxCandidates: Int,
        suppressionThetaRadius: Int = DEFAULT_SUPPRESSION_THETA_RADIUS,
        suppressionRhoRadius: Int = DEFAULT_SUPPRESSION_RHO_RADIUS
    ): List<LineCandidate> {
        if (maxCandidates <= 0) {
            return emptyList()
        }

        val suppressedAccumulator = accumulatorBins.copyOf()
        val selectedPeaks = mutableListOf<AccumulatorPeak>()

        repeat(maxCandidates) {
            val peak = findStrongestAccumulatorPeak(suppressedAccumulator) ?: return@repeat
            if (peak.score <= 0f) {
                return@repeat
            }

            selectedPeaks.add(peak)
            suppressNeighborhood(
                suppressedAccumulator,
                peak.thetaBinIndex,
                peak.rhoBinIndex,
                suppressionThetaRadius,
                suppressionRhoRadius
            )
        }

        return selectedPeaks.map {
            LineCandidate(getLineCandidate(it.thetaBinIndex, it.rhoBinIndex), it.score)
        }
    }

    private fun findStrongestAccumulatorPeak(accumulator: FloatArray): AccumulatorPeak? {
        var bestScore = 0f
        var bestThetaIndex = -1
        var bestRhoIndex = -1

        for (thetaIndex in 0..<thetaBinCount) {
            for (rhoIndex in 0..<rhoBinCount) {
                val score = accumulator[thetaIndex * rhoBinCount + rhoIndex]
                if (score > bestScore) {
                    bestScore = score
                    bestThetaIndex = thetaIndex
                    bestRhoIndex = rhoIndex
                }
            }
        }

        if (bestThetaIndex == -1 || bestRhoIndex == -1) {
            return null
        }

        return AccumulatorPeak(bestThetaIndex, bestRhoIndex, bestScore)
    }

    private fun suppressNeighborhood(
        accumulator: FloatArray,
        thetaBinIndex: Int,
        rhoBinIndex: Int,
        suppressionThetaRadius: Int,
        suppressionRhoRadius: Int
    ) {
        for (neighborTheta in max(0, thetaBinIndex - suppressionThetaRadius)..min(
            thetaBinCount - 1,
            thetaBinIndex + suppressionThetaRadius
        )) {
            for (neighborRho in max(0, rhoBinIndex - suppressionRhoRadius)..min(
                rhoBinCount - 1,
                rhoBinIndex + suppressionRhoRadius
            )) {
                accumulator[neighborTheta * rhoBinCount + neighborRho] = 0f
            }
        }
    }

    fun getLineCandidate(thetaBinIndex: Int, rhoBinIndex: Int): PolarLine {
        return PolarLine(getRho(rhoBinIndex), thetaValues[thetaBinIndex])
    }

    private data class AccumulatorPeak(
        val thetaBinIndex: Int,
        val rhoBinIndex: Int,
        val score: Float
    )

    companion object {
        private const val DEFAULT_SUPPRESSION_THETA_RADIUS = 2
        private const val DEFAULT_SUPPRESSION_RHO_RADIUS = 8
    }
}

data class LineCandidate(val line: PolarLine, val score: Float)