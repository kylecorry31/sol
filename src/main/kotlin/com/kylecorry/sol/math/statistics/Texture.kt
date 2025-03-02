package com.kylecorry.sol.math.statistics

import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.algebra.Matrix
import com.kylecorry.sol.math.algebra.columns
import com.kylecorry.sol.math.algebra.rows
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

object Texture {

    fun features(glcm: Matrix): TextureFeatures {
        var entropy = 0f
        var contrast = 0f
        var homogeneity = 0f
        var dissimilarity = 0f
        var angularSecondMoment = 0f
        var meanI = 0f
        var meanJ = 0f
        var maximum = 0f
        var varianceI = 0f
        var varianceJ = 0f
        var correlation = 0f

        // Texture measures and mean
        for (i in 0..<glcm.rows()) {
            for (j in 0..<glcm.columns()) {
                val p = glcm[i][j]
                val ijSquare = square((i - j).toFloat())
                angularSecondMoment += square(p)
                if (p > 0) {
                    entropy += -p * ln(p)
                }
                contrast += ijSquare * p
                homogeneity += p / (1 + ijSquare)
                dissimilarity += p * abs(i - j)
                maximum = max(maximum, p)
                meanI += i * p
                meanJ += j * p
            }
        }

        // Variance calculation
        for (i in 0..<glcm.rows()) {
            for (j in 0..<glcm.columns()) {
                val p = glcm[i][j]
                varianceI += p * square(i - meanI)
                varianceJ += p * square(j - meanJ)
                correlation += p * (i - meanI) * (j - meanJ)
            }
        }

        // Correlation calculation
        val denominator = sqrt(varianceI * varianceJ)
        if (denominator != 0f) {
            correlation /= denominator
        }

        return TextureFeatures(
            sqrt(angularSecondMoment),
            entropy,
            contrast,
            homogeneity,
            dissimilarity,
            angularSecondMoment,
            meanI,
            meanJ,
            varianceI,
            varianceJ,
            correlation,
            maximum
        )
    }

}