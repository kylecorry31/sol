package com.kylecorry.sol.math.statistics

import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.algebra.Matrix
import kotlin.math.abs
import kotlin.math.log2

object GLCM {

    fun features(glcm: Matrix): GLCMFeatures {
        var energy = 0f
        var entropy = 0f
        var contrast = 0f
        var homogeneity = 0f
        for (a in glcm.indices) {
            for (b in glcm[0].indices) {
                val p = glcm[a][b]
                energy += square(p)
                if (p > 0) {
                    entropy += -p * log2(p)
                }
                contrast += square((a - b).toFloat()) * p
                homogeneity += p / (1 + abs(a - b))
            }
        }
        return GLCMFeatures(energy, entropy, contrast, homogeneity)
    }

    fun energy(glcm: Matrix): Float {
        return features(glcm).energy
    }

    fun entropy(glcm: Matrix): Float {
        return features(glcm).entropy
    }

    fun contrast(glcm: Matrix): Float {
        return features(glcm).contrast
    }

    fun homogeneity(glcm: Matrix): Float {
        return features(glcm).homogeneity
    }

}