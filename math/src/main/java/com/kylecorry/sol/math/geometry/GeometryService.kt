package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.algebra.AlgebraService
import kotlin.math.sqrt

class GeometryService {

    private val algebra = AlgebraService()
    private val intersection = IntersectionService(algebra)

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

                Line(start, end)
            } else {
                null
            }
        }

        val intersection = intersection.getIntersection(line, circle) ?: return null
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

        return Line(start, end)
    }

    fun getIntersection(line1: Line, line2: Line): Vector2? {
        return intersection.getIntersection(line1, line2)
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
            return point.x == line.start.x && point.y in line.bottom().y..line.top().y
        }

        return line.equation().evaluate(
            point.x
        ) == point.y && point.x in line.left().x..line.right().x
    }

    fun contains(circle: Circle, point: Vector2): Boolean {
        return circle.center.distanceTo(point) <= circle.radius
    }
}