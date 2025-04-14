package com.kylecorry.sol.math.interpolation

class LinearInterpolator {

    fun interpolate(
        x: Float,
        xs: List<Float>,
        ys: List<Float>
    ): Float {
        val beforeIndex = xs.indexOfLast { it <= x }
        val afterIndex = xs.indexOfFirst { it >= x }

        if (beforeIndex == -1) {
            return ys.first()
        }

        if (afterIndex == -1) {
            return ys.last()
        }

        if (beforeIndex == afterIndex) {
            return ys[beforeIndex]
        }

        val before = xs[beforeIndex]
        val after = xs[afterIndex]

        val beforeY = ys[beforeIndex]
        val afterY = ys[afterIndex]

        return Interpolation.linear(x, before, beforeY, after, afterY)
    }

}