package com.kylecorry.sol.science.astronomy.stars

class Constellation(val name: String, val lines: List<List<Int>>) {
    val allStarIds = lines.flatten().toSet()

    val starEdges: List<Pair<Star, Star>>
        get() {
            return lines.flatMap { line ->
                line.zipWithNext { a, b ->
                    Pair(star(a), star(b))
                }
            }
        }

    fun containsStar(star: Star): Boolean {
        return allStarIds.contains(star.hipDesignation)
    }
}

private fun star(hipDesignation: Int): Star {
    return STAR_CATALOG_BY_HIP[hipDesignation]!!
}