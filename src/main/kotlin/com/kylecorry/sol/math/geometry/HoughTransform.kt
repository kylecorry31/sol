package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.MathExtensions.toRadians
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

object HoughTransform {
    fun vote(
        gradients: Gradients,
        threshold: Float,
        thetaBinCount: Int,
        startThetaDegrees: Float = 0f,
        endThetaDegrees: Float = 180f
    ): HoughLineParameterSpace {
        require(thetaBinCount > 0) { "thetaBinCount must be greater than 0" }

        val imageWidth = gradients.magnitude.columns()
        val imageHeight = gradients.magnitude.rows()

        val maxRho = hypot(imageWidth.toFloat(), imageHeight.toFloat())
        val rhoBinCount = max(1, (maxRho * 2).roundToInt() + 1)
        val accumulatorBins = FloatArray(thetaBinCount * rhoBinCount)

        val thetaValues = FloatArray(thetaBinCount)
        val cosThetaValues = FloatArray(thetaBinCount)
        val sinThetaValues = FloatArray(thetaBinCount)

        for (thetaBinIndex in 0..<thetaBinCount) {
            val thetaDeg = if (thetaBinCount == 1) {
                (startThetaDegrees + endThetaDegrees) / 2f
            } else {
                startThetaDegrees +
                        (endThetaDegrees - startThetaDegrees) * thetaBinIndex / (thetaBinCount - 1).toFloat()
            }
            val theta = thetaDeg.toRadians()
            thetaValues[thetaBinIndex] = theta
            cosThetaValues[thetaBinIndex] = cos(theta)
            sinThetaValues[thetaBinIndex] = sin(theta)
        }

        for (y in 0..<imageHeight) {
            for (x in 0..<imageWidth) {
                val magnitude = gradients.magnitude[y, x]
                if (magnitude < threshold) {
                    continue
                }

                castVotes(
                    x.toFloat(),
                    y.toFloat(),
                    magnitude,
                    thetaBinCount,
                    rhoBinCount,
                    maxRho,
                    cosThetaValues,
                    sinThetaValues,
                    accumulatorBins
                )
            }
        }

        return HoughLineParameterSpace(
            accumulatorBins,
            rhoBinCount,
            thetaBinCount,
            maxRho,
            thetaValues
        )
    }

    private fun castVotes(
        x: Float,
        y: Float,
        weight: Float,
        thetaBinCount: Int,
        rhoBinCount: Int,
        maxRho: Float,
        cosThetaValues: FloatArray,
        sinThetaValues: FloatArray,
        accumulatorBins: FloatArray
    ) {
        for (thetaBinIndex in 0..<thetaBinCount) {
            val rho = x * cosThetaValues[thetaBinIndex] +
                    y * sinThetaValues[thetaBinIndex]

            val rhoBinIndex = rhoToBinIndex(rho, maxRho, rhoBinCount)
            val accumulatorIndex = thetaBinIndex * rhoBinCount + rhoBinIndex
            accumulatorBins[accumulatorIndex] += weight
        }
    }

    private fun rhoToBinIndex(
        rho: Float,
        maxRho: Float,
        rhoBinCount: Int
    ): Int {
        return (((rho + maxRho) / (2f * maxRho)) * (rhoBinCount - 1))
            .roundToInt()
            .coerceIn(0, rhoBinCount - 1)
    }
}
