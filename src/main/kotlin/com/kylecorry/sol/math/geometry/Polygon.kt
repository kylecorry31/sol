package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.arithmetic.Arithmetic
import kotlin.math.absoluteValue

data class Polygon(val vertices: List<Vector2>) {
    val edges: List<Line>
        get() {
            val edges = mutableListOf<Line>()
            for (i in vertices.indices) {
                val start = vertices[i]
                val end = vertices[(i + 1) % vertices.size]
                edges.add(Line(start, end))
            }
            return edges
        }

    fun area(): Float {
        return signedArea().absoluteValue
    }

    fun signedArea(): Float {
        var area2 = 0f
        for (i in vertices.indices) {
            val next = vertices[(i + 1) % vertices.size]
            area2 += vertices[i].x * next.y - next.x * vertices[i].y
        }
        return area2 / 2f
    }

    fun centroid(): Vector2 {
        var cx = 0f
        var cy = 0f
        var area2 = 0f
        for (i in vertices.indices) {
            val next = vertices[(i + 1) % vertices.size]
            val cross = vertices[i].x * next.y - next.x * vertices[i].y
            area2 += cross
            cx += (vertices[i].x + next.x) * cross
            cy += (vertices[i].y + next.y) * cross
        }
        val area = area2 / 2
        if (Arithmetic.isZero(area)) {
            return vertices.firstOrNull() ?: Vector2.zero
        }

        return Vector2(cx / (6f * area), cy / (6f * area))
    }
}
