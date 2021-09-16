package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.algebra.AlgebraService
import com.kylecorry.sol.math.Vector2
import kotlin.math.atan2

class GeometryService {

    private val algebra = AlgebraService()

    fun getIntersection(line: Line, circle: Circle): Pair<Vector2, Vector2>? {
        val m = line.slope()
        val yIntercept = line.intercept()
        val a = (1 - m * m)
        val b = -(2 * circle.center.x + 2 * m * yIntercept - 2 * circle.center.y * m)
        val c =
            square(circle.center.x) + square(yIntercept) - 2 * circle.center.y * yIntercept + square(
                circle.center.y - circle.radius
            )

        val solutions = algebra.solveQuadratic(a, b, c) ?: return null

        val y1 = algebra.evaluateLinear(solutions.first, m, yIntercept)
        val y2 = algebra.evaluateLinear(solutions.second, m, yIntercept)

        return Vector2(solutions.first, y1) to Vector2(solutions.second, y2)
    }

    fun getIntersection(line1: Line, line2: Line): Vector2? {
        val intercepts = line1.intercept() - line2.intercept()
        val slopes = line2.slope() - line1.slope()

        if (slopes == 0f) {
            return null
        }

        val x = intercepts / slopes
        return Vector2(x, algebra.evaluateLinear(x, line1.slope(), line1.intercept()))
    }

    fun getAngle(line1: Line, line2: Line): Float {
        val m1 = line1.slope()
        val m2 = line2.slope()
        return atan2(m1 - m2, 1 + m1 * m2).toDegrees()
    }

    // TODO: Add contains methods
    // TODO: Add intersects methods


}