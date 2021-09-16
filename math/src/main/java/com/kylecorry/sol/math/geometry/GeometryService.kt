package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.algebra.AlgebraService

class GeometryService {

    private val algebra = AlgebraService()
    private val intersection = IntersectionService(algebra)

    // INTERSECTIONS

    fun getIntersection(line: Line, circle: Circle): Pair<Vector2, Vector2>? {
        return intersection.getIntersection(line, circle)
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
        return algebra.evaluateLinear(point.x, line.slope(), line.intercept()) == point.y
    }

    fun contains(circle: Circle, point: Vector2): Boolean {
        return circle.center.distanceTo(point) <= circle.radius
    }
}