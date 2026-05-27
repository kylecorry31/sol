package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.MathExtensions.toRadians
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

object HoughTransform {
    fun vote(
        featurePoints: List<WeightedPoint>,
        imageWidth: Int,
        imageHeight: Int,
        thetaBinCount: Int,
        startThetaDegrees: Float,
        endThetaDegrees: Float
    ): HoughLineParameterSpace {
        require(thetaBinCount > 0) { "thetaBins must be greater than 0" }
        require(imageWidth > 0) { "imageWidth must be greater than 0" }
        require(imageHeight > 0) { "imageHeight must be greater than 0" }

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
                startThetaDegrees + (endThetaDegrees - startThetaDegrees) * thetaBinIndex / (thetaBinCount - 1).toFloat()
            }
            val theta = thetaDeg.toRadians()
            thetaValues[thetaBinIndex] = theta
            cosThetaValues[thetaBinIndex] = cos(theta)
            sinThetaValues[thetaBinIndex] = sin(theta)
        }

        for (featurePoint in featurePoints) {
            castVotes(featurePoint, thetaBinCount, rhoBinCount, maxRho, cosThetaValues, sinThetaValues, accumulatorBins)
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
        featurePoint: WeightedPoint,
        thetaBinCount: Int,
        rhoBinCount: Int,
        maxRho: Float,
        cosThetaValues: FloatArray,
        sinThetaValues: FloatArray,
        accumulatorBins: FloatArray
    ) {
        for (thetaBinIndex in 0..<thetaBinCount) {
            val rho = featurePoint.point.x * cosThetaValues[thetaBinIndex] +
                    featurePoint.point.y * sinThetaValues[thetaBinIndex]

            val rhoBinIndex = rhoToBinIndex(rho, maxRho, rhoBinCount)

            val accumulatorIndex = thetaBinIndex * rhoBinCount + rhoBinIndex
            accumulatorBins[accumulatorIndex] += featurePoint.weight
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
