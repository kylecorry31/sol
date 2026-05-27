package com.kylecorry.sol.math.geometry

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

    fun findStrongestLineCandidate(): PolarLine? {
        var bestScore = 0f
        var bestThetaIndex = -1
        var bestRhoIndex = -1

        for (thetaIndex in 0..<thetaBinCount) {
            for (rhoIndex in 0..<rhoBinCount) {
                val score = score(thetaIndex, rhoIndex)
                if (score > bestScore) {
                    bestScore = score
                    bestThetaIndex = thetaIndex
                    bestRhoIndex = rhoIndex
                }
            }
        }

        if (bestThetaIndex == -1) {
            return null
        }

        return getLineCandidate(bestThetaIndex, bestRhoIndex)
    }

    fun getLineCandidate(thetaBinIndex: Int, rhoBinIndex: Int): PolarLine {
        return PolarLine(getRho(rhoBinIndex), thetaValues[thetaBinIndex])
    }
}
