package com.kylecorry.sol.math.regression

import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.algebra.LinearEquation
import com.kylecorry.sol.math.statistics.Statistics

class LinearRegression(data: List<Vector2>) : IRegression1D {

    val equation = fit(data)

    override fun predict(x: Float): Float {
        return equation.evaluate(x)
    }

    // The linear case is more efficient than the matrix operations
    private fun fit(data: List<Vector2>): LinearEquation {
        if (data.size <= 1) {
            return LinearEquation(0f, 0f)
        }

        val mean = Statistics.meanVector2(data)
        val xBar = mean.x
        val yBar = mean.y

        var ssxx = 0.0f
        var ssxy = 0.0f
        var ssto = 0.0f

        for (i in data.indices) {
            val x = data[i].x
            val y = data[i].y
            ssxx += square(x - xBar)
            ssxy += (x - xBar) * (y - yBar)
            ssto += square(y - yBar)
        }

        val slope = ssxy / ssxx
        val intercept = yBar - xBar * slope
        return LinearEquation(slope, intercept)
    }

}