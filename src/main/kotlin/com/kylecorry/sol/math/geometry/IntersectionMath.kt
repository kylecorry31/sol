package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.algebra.Algebra
import com.kylecorry.sol.math.algebra.QuadraticEquation
import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.arithmetic.Arithmetic.square
import kotlin.math.absoluteValue

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
        val p = line1.start
        val r = line1.end - line1.start
        val q = line2.start
        val s = line2.end - line2.start

        val denominator = cross(r, s)
        if (Arithmetic.isZero(denominator)) {
            return null
        }

        val qMinusP = q - p
        val t = cross(qMinusP, s) / denominator
        val u = cross(qMinusP, r) / denominator

        if (!isBetweenZeroAndOne(t) || !isBetweenZeroAndOne(u)) {
            return null
        }

        return Vector2(p.x + t * r.x, p.y + t * r.y)
    }

    fun getIntersection(line: Line, rectangle: Rectangle): List<Vector2> {

        // If both the start and end are on the same side of the rectangle and outside of it, there is no intersection
        if (line.start.x < rectangle.left && line.end.x < rectangle.left ||
            line.start.x > rectangle.right && line.end.x > rectangle.right ||
            line.start.y < rectangle.bottom && line.end.y < rectangle.bottom ||
            line.start.y > rectangle.top && line.end.y > rectangle.top
        ) {
            return emptyList()
        }


        if (line.isVertical) {
            val x = line.start.x
            if (x !in rectangle.left..rectangle.right) {
                return emptyList()
            }
            val top = Vector2(x, rectangle.top)
            val bottom = Vector2(x, rectangle.bottom)
            return listOf(top, bottom)
        }

        val equation = line.equation()
        val inverse = if (line.isHorizontal) {
            // Inverse is not defined for horizontal lines
            null
        } else {
            Algebra.inverse(equation)
        }
        val left = Vector2(rectangle.left, equation.evaluate(rectangle.left))
        val right = Vector2(rectangle.right, equation.evaluate(rectangle.right))
        val top = inverse?.let { Vector2(inverse.evaluate(rectangle.top), rectangle.top) }
        val bottom = inverse?.let { Vector2(inverse.evaluate(rectangle.bottom), rectangle.bottom) }

        val intersections = mutableSetOf<Vector2>()

        if (rectangle.contains(left)) {
            intersections.add(left)
        }

        if (rectangle.contains(right)) {
            intersections.add(right)
        }

        if (top != null && rectangle.contains(top)) {
            intersections.add(top)
        }

        if (bottom != null && rectangle.contains(bottom)) {
            intersections.add(bottom)
        }

        return intersections.toList()
    }

    fun getIntersection(a: Vector2, b: Vector2, rectangle: Rectangle): List<Vector2> {
        val line = Line(a, b)
        val intersection = getIntersection(line, rectangle)
        // Only include the intersection if it's on the line segment between A and B
        val minX = minOf(a.x, b.x)
        val maxX = maxOf(a.x, b.x)
        val minY = minOf(a.y, b.y)
        val maxY = maxOf(a.y, b.y)
        return intersection.filter { it.x in minX..maxX && it.y in minY..maxY }
    }

    private fun cross(a: Vector2, b: Vector2): Float {
        return a.x * b.y - a.y * b.x
    }

    private fun isBetweenZeroAndOne(value: Float, tolerance: Float = 0.00001f): Boolean {
        return value >= -tolerance && value <= 1f + tolerance || value.absoluteValue < tolerance
    }
}
