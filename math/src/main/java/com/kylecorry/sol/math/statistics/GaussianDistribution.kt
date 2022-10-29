package com.kylecorry.sol.math.statistics

import com.kylecorry.sol.math.SolMath.square

data class GaussianDistribution(val mean: Float, val standardDeviation: Float){
    val variance = square(standardDeviation)
}
