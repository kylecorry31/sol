package com.kylecorry.sol.math.statistics

import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.algebra.Matrix
import com.kylecorry.sol.math.algebra.columns
import com.kylecorry.sol.math.algebra.rows
import kotlin.math.abs
import kotlin.math.log2

object Texture {

    fun features(glcm: Matrix): TextureFeatures {
        var energy = 0f
        var entropy = 0f
        var contrast = 0f
        var homogeneity = 0f
        var dissimilarity = 0f
        var angularSecondMoment = 0f
        for (a in 0 until glcm.rows()) {
            for (b in 0 until glcm.columns()) {
                val p = glcm[a][b]
                val abSquare = square((a - b).toFloat())
                angularSecondMoment += square(p)
                energy += square(p)
                if (p > 0) {
                    entropy += -p * log2(p)
                }
                contrast += abSquare * p
                homogeneity += p / (1 + abSquare)
                dissimilarity += p * abs(a - b)
            }
        }
        return TextureFeatures(
            energy,
            entropy,
            contrast,
            homogeneity,
            dissimilarity,
            angularSecondMoment
        )
    }

}