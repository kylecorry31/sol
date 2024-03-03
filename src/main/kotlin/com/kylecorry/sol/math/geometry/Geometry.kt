package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.math.sumOfFloat
import kotlin.math.*

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

    // Area

    fun getIntersectionArea(circle1: Circle, circle2: Circle): Float {
        // No intersection
        val d = circle1.center.distanceTo(circle2.center)
        if (d >= circle1.radius + circle2.radius) {
            return 0f
        }

        // One circle is inside the other
        if (d <= abs(circle1.radius - circle2.radius)) {
            val r = minOf(circle1.radius, circle2.radius)
            return PI.toFloat() * square(r)
        }

        val r1 = circle1.radius
        val r2 = circle2.radius

        // First sector area
        val a1 = square(r1) * acos((square(d) + square(r1) - square(r2)) / (2 * d * r1))
        // Second sector area
        val a2 = square(r2) * acos((square(d) + square(r2) - square(r1)) / (2 * d * r2))

        // Triangle area
        val a3 = 0.5f * sqrt((-d + r1 + r2) * (d + r1 - r2) * (d - r1 + r2) * (d + r1 + r2))

        // Sectors - triangle
        return a1 + a2 - a3
    }

    // Snapping

    /**
     * Snap a 2D point onto the nearest part of a line
     * @param point the point
     * @param line the line
     * @return the point snapped onto the line
     */
    fun snapToLine(point: Vector2, line: Line): Vector2 {
        return snapToLine(point.x, point.y, line.start.x, line.start.y, line.end.x, line.end.y)
    }

    /**
     * Snap a 2D point onto the nearest part of a line
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @param x1 the x coordinate of the start of the line
     * @param y1 the y coordinate of the start of the line
     * @param x2 the x coordinate of the end of the line
     * @param y2 the y coordinate of the end of the line
     * @return the point snapped onto the line
     */
    fun snapToLine(x: Float, y: Float, x1: Float, y1: Float, x2: Float, y2: Float): Vector2 {
        val ab = square(x2 - x1) + square(y2 - y1)
        val ap = square(x - x1) + square(y - y1)
        val bp = square(x - x2) + square(y - y2)

        val t = ((ap - bp + ab) / (2 * ab)).coerceIn(0f, 1f)
        val projectedX = x1 + t * (x2 - x1)
        val projectedY = y1 + t * (y2 - y1)
        return Vector2(projectedX, projectedY)
    }

    /**
     * Snap a 2D point onto the nearest part of a 3D line
     * @param point the point
     * @param x1 the x coordinate of the start of the line
     * @param y1 the y coordinate of the start of the line
     * @param z1 the z coordinate of the start of the line
     * @param x2 the x coordinate of the end of the line
     * @param y2 the y coordinate of the end of the line
     * @param z2 the z coordinate of the end of the line
     * @return the 3D point snapped onto the line
     */
    fun snapTo3DLine(point: Vector2, x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): Vector3 {
        return snapTo3DLine(point.x, point.y, x1, y1, z1, x2, y2, z2)
    }

    /**
     * Snap a 2D point onto the nearest part of a 3D line
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @param x1 the x coordinate of the start of the line
     * @param y1 the y coordinate of the start of the line
     * @param z1 the z coordinate of the start of the line
     * @param x2 the x coordinate of the end of the line
     * @param y2 the y coordinate of the end of the line
     * @param z2 the z coordinate of the end of the line
     * @return the 3D point snapped onto the line
     */
    fun snapTo3DLine(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        z1: Float,
        x2: Float,
        y2: Float,
        z2: Float
    ): Vector3 {
        val ab = square(x2 - x1) + square(y2 - y1)
        val ap = square(x - x1) + square(y - y1)
        val bp = square(x - x2) + square(y - y2)

        val t = ((ap - bp + ab) / (2 * ab)).coerceIn(0f, 1f)
        val projectedX = x1 + t * (x2 - x1)
        val projectedY = y1 + t * (y2 - y1)
        val projectedZ = z1 + t * (z2 - z1)
        return Vector3(projectedX, projectedY, projectedZ)
    }

}