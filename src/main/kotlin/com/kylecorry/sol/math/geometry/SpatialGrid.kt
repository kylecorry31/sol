package com.kylecorry.sol.math.geometry

import kotlin.math.floor

/**
 * A grid index for finding the indexed points near a point without comparing it to every one. Each lookup
 * visits 3^dimensions cells, so it is intended for a small number of dimensions.
 *
 * @param points the points to index, all with the same number of coordinates
 * @param cellSize the size of a cell in every dimension, must be greater than 0
 */
class SpatialGrid(points: List<List<Float>>, val cellSize: Float) {

    private val dimensions = points.firstOrNull()?.size ?: 0
    private val cells = HashMap<List<Long>, MutableList<Int>>()

    val size = points.size

    init {
        require(cellSize > 0f) { "The cell size must be greater than 0" }
        for (i in points.indices) {
            cells.getOrPut(getCell(points[i])) { mutableListOf() }.add(i)
        }
    }

    /**
     * Gets the indices of the points that could be within [cellSize] of the point in every coordinate. All
     * such points are returned, along with some that are not, so check the actual distance.
     */
    fun getCandidates(point: List<Float>): List<Int> {
        val candidates = IntArray(size)
        return candidates.asList().subList(0, getCandidates(point, candidates))
    }

    /**
     * Writes the candidates to [out] (which must hold [size] indices) instead of allocating a list.
     * @return the number of indices written
     */
    fun getCandidates(point: List<Float>, out: IntArray): Int {
        if (dimensions == 0) {
            return 0
        }

        val center = getCell(point)
        // Offset of the visited cell from the center, counted in base 3 over (-1, 0, 1) per dimension
        val offsets = LongArray(dimensions) { -1L }
        val key = LongArray(dimensions)
        var count = 0
        while (true) {
            for (d in 0 until dimensions) {
                key[d] = center[d] + offsets[d]
            }
            val bucket = cells[key.asList()]
            if (bucket != null) {
                for (index in bucket) {
                    out[count++] = index
                }
            }

            var d = 0
            while (d < dimensions && offsets[d] == 1L) {
                offsets[d] = -1L
                d++
            }
            if (d == dimensions) {
                return count
            }
            offsets[d]++
        }
    }

    private fun getCell(point: List<Float>): List<Long> {
        return List(dimensions) { floor(point[it] / cellSize).toLong() }
    }
}
