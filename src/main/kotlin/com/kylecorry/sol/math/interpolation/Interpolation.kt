package com.kylecorry.sol.math.interpolation

import com.kylecorry.sol.math.Vector2

object Interpolation {

    /**
     * Calculates a linear interpolation
     * @param x: The x value to interpolate
     * @param point1: The first control point (to the left of x)
     * @param point2: The second control point (to the right of x)
     * @return The y value of the interpolation
     */
    fun linear(x: Float, point1: Vector2, point2: Vector2): Float {
        val x1 = point1.x
        val y1 = point1.y
        val x2 = point2.x
        val y2 = point2.y
        return linear(x, x1, y1, x2, y2)
    }

    /**
     * Calculates a linear interpolation
     * @param x: The x value to interpolate
     * @param x1: The x value of the first control point (to the left of x)
     * @param y1: The y value of the first control point (to the left of x)
     * @param x2: The x value of the second control point (to the right of x)
     * @param y2: The y value of the second control point (to the right of x)
     * @return The y value of the interpolation
     */
    fun linear(x: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return y1 + (x - x1) * (y2 - y1) / (x2 - x1)
    }

    /**
     * Calculates a cubic interpolation
     * @param x: The x value to approximate
     * @param point0: The first control point (to the left of point 1)
     * @param point1: The second control point (to the left of x)
     * @param point2: The third control point (to the right of x)
     * @param point3: The fourth control point (to the right of point 2)
     * @return The y value of the approximation
     */
    fun cubic(
        x: Float,
        point0: Vector2,
        point1: Vector2,
        point2: Vector2,
        point3: Vector2
    ): Float {
        val x0 = point0.x
        val y0 = point0.y
        val x1 = point1.x
        val y1 = point1.y
        val x2 = point2.x
        val y2 = point2.y
        val x3 = point3.x
        val y3 = point3.y

        return cubic(x, x0, y0, x1, y1, x2, y2, x3, y3)
    }

    /**
     * Calculates a cubic interpolation
     * @param x: The x value to approximate
     * @param x0: The x value of the first control point (to the left of x1)
     * @param y0: The y value of the first control point (to the left of x1)
     * @param x1: The x value of the second control point (to the left of x)
     * @param y1: The y value of the second control point (to the left of x)
     * @param x2: The x value of the third control point (to the right of x)
     * @param y2: The y value of the third control point (to the right of x)
     * @param x3: The x value of the fourth control point (to the right of x2)
     * @param y3: The y value of the fourth control point (to the right of x2)
     * @return The y value of the approximation
     */
    fun cubic(
        x: Float,
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float
    ): Float {
        return interpolate(
            x,
            listOf(x0, x1, x2, x3),
            listOf(y0, y1, y2, y3)
        )
    }

    /**
     * Use Lagrange interpolation to interpolate a value. You need at least order + 1 control points.
     * If multiple points are to be interpolated, use [NewtonInterpolator] since it caches the coefficients.
     * @param x: The x value to interpolate
     * @param xs: The x values of the control points
     * @param ys: The y values of the control points
     * @param order: The order of the interpolation (default is xs.size - 1)
     * @return The y value of the interpolation
     */
    fun interpolate(
        x: Float,
        xs: List<Float>,
        ys: List<Float>,
        order: Int = xs.size - 1
    ): Float {
        var y = 0f
        for (i in 0..order) {
            var p = 1f
            for (j in 0..order) {
                if (i != j) {
                    p *= (x - xs[j]) / (xs[i] - xs[j])
                }
            }
            y += p * ys[i]
        }
        return y
    }

}

