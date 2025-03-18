package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2

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
}