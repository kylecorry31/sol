package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.sumOfFloat
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sqrt

object Geometry {
    // INTERSECTIONS

    fun getIntersection(line: Line, circle: Circle): Line? {

        if (contains(circle, line.start) && contains(circle, line.end)) {
            return line
        }

        // Vertical line
        if (line.isVertical) {
            return if (circle.contains(Vector2(line.start.x, circle.center.y))) {
                val top = Vector2(
                    line.start.x,
                    sqrt(square(circle.radius) - square(line.start.x - circle.center.x)) + circle.center.y
                )
                val bottom = top.copy(y = -top.y)
                val lineTop = line.top()
                val lineBottom = line.bottom()

                val start = if (contains(circle, lineTop)) {
                    lineTop
                } else {
                    top
                }

                val end = if (contains(circle, lineBottom)) {
                    lineBottom
                } else {
                    bottom
                }

                if (!contains(line, start) || !contains(line, end)) {
                    return null
                }

                Line(start, end)
            } else {
                null
            }
        }

        val intersection = IntersectionMath.getIntersection(line, circle) ?: return null
        val intersectionLine = Line(intersection.first, intersection.second)

        val left = intersectionLine.left()
        val right = intersectionLine.right()

        val lineLeft = line.left()
        val lineRight = line.right()

        val start = if (contains(circle, lineLeft)) {
            lineLeft
        } else {
            left
        }

        val end = if (contains(circle, lineRight)) {
            lineRight
        } else {
            right
        }

        if (!contains(line, start) || !contains(line, end)) {
            return null
        }

        return Line(start, end)
    }

    fun getIntersection(line1: Line, line2: Line): Vector2? {
        return IntersectionMath.getIntersection(line1, line2)
    }

    fun getIntersection(line: Line, rectangle: Rectangle): List<Vector2> {
        return IntersectionMath.getIntersection(line, rectangle)
    }

    fun getIntersection(a: Vector2, b: Vector2, rectangle: Rectangle): List<Vector2> {
        return IntersectionMath.getIntersection(a, b, rectangle)
    }

    fun intersects(line: Line, circle: Circle): Boolean {
        return getIntersection(line, circle) != null
    }

    fun intersects(line1: Line, line2: Line): Boolean {
        return getIntersection(line1, line2) != null
    }

    // CONTAINS

    fun contains(line: Line, point: Vector2): Boolean {
        // Vertical line
        if (line.isVertical) {
            return point.x == line.start.x && point.y >= line.bottom().y && point.y <= line.top().y
        }

        val eval = line.equation().evaluate(point.x)

        return (eval - point.y).absoluteValue < 0.00001f && point.x >= line.left().x && point.x <= line.right().x
    }

    fun contains(circle: Circle, point: Vector2): Boolean {
        return circle.center.distanceTo(point) <= circle.radius
    }

    fun pointLineDistance(point: Vector2, line: Line): Float {
        if (line.start == line.end) {
            return point.distanceTo(line.start)
        }

        val numerator =
            abs((line.end.x - line.start.x) * (line.start.y - point.y) - (line.start.x - point.x) * (line.end.y - line.start.y))
        val denominator = line.length()

        return numerator / denominator
    }

    fun manhattanDistance(p1: List<Float>, p2: List<Float>): Float {
        return p1.zip(p2).sumOfFloat { abs(it.first - it.second) }
    }

    fun euclideanDistance(p1: List<Float>, p2: List<Float>): Float {
        return sqrt(p1.zip(p2).sumOfFloat { square(it.first - it.second) })
    }
}