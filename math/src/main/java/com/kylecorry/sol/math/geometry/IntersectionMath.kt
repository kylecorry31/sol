package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.algebra.Algebra
import com.kylecorry.sol.math.algebra.QuadraticEquation

internal object IntersectionMath {

    fun getIntersection(line: Line, circle: Circle): Pair<Vector2, Vector2>? {
        val centeredLine = Line(line.start - circle.center, line.end - circle.center)

        val m = centeredLine.slope()
        val yIntercept = centeredLine.intercept()
        val a = (1 + square(m))
        val b = 2 * yIntercept * m
        val c = square(yIntercept) - square(circle.radius)
        val solutions = Algebra.solve(QuadraticEquation(a, b, c)) ?: return null

        val y1 = centeredLine.equation().evaluate(solutions.first)
        val y2 = centeredLine.equation().evaluate(solutions.second)

        val start = Vector2(solutions.first, y1) + circle.center
        val end = Vector2(solutions.second, y2) + circle.center

        return start to end
    }

    fun getIntersection(line1: Line, line2: Line): Vector2? {
        val intercepts = line1.intercept() - line2.intercept()
        val slopes = line2.slope() - line1.slope()

        if (slopes == 0f) {
            return null
        }

        val x = intercepts / slopes
        return Vector2(x, line1.equation().evaluate(x))
    }

    /**
     * Line and rectangle - just evaluate at x = left, right and y = top, bottom
     */

}