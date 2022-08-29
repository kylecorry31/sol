package com.kylecorry.sol.math.regression

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.algebra.LinearEquation
import kotlin.math.pow

class LinearRegression(data: List<Vector2>): IRegression {

    val equation = fit(data)

    override fun predict(x: Float): Float {
        return equation.evaluate(x)
    }

    private fun fit(data: List<Vector2>): LinearEquation {
        if (data.size <= 1) {
            return LinearEquation(0f, 0f)
        }

        val xBar = data.map { it.x }.average().toFloat()
        val yBar = data.map { it.y }.average().toFloat()

        var ssxx = 0.0f
        var ssxy = 0.0f
        var ssto = 0.0f

        for (i in data.indices) {
            val x = data[i].x
            val y = data[i].y
            ssxx += (x - xBar).pow(2)
            ssxy += (x - xBar) * (y - yBar)
            ssto += (y - yBar).pow(2)
        }

        val slope = ssxy / ssxx
        val intercept = yBar - xBar * slope
        return LinearEquation(slope, intercept)
    }

}