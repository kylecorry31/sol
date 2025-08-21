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

val CONSTELLATIONS = listOf(
    Constellation("Andromeda", listOf(listOf(9640, 5447, 3092, 677), listOf(3092, 3031, 3693, 4463), listOf(3092, 2912, 116631, 116805, 116584), listOf(116631, 113726), listOf(5447, 2912), listOf(5447, 4436, 3881, 5434, 7607))),
    Constellation("Antila", listOf(listOf(53502, 51172, 46515))),
    Constellation("Apus", listOf(listOf(81065, 81852, 80047, 72370))),
    Constellation("Aquarius", listOf(listOf(102618, 103045, 106278, 109074, 110672, 110960, 110395, 109074, 110003, 110273, 112961, 112716, 113136), listOf(109139, 106278), listOf(110960, 111497), listOf(112961, 114724))),
    Constellation("Aquila", listOf(listOf(98036, 97649, 97278, 95501, 93805), listOf(99473, 97804, 95501, 93747, 93244), listOf(99473, 96468, 93805), listOf(93805, 93747))),
    Constellation("Ara", listOf(listOf(88714, 85792, 85312, 83153, 83081, 82363, 85727, 85267), listOf(85258, 85792))),
    Constellation("Aries", listOf(listOf(8832, 8903, 9884, 13209))),
    Constellation("Auriga", listOf(listOf(24608, 28358, 28404, 28360, 28380, 25428, 23015, 23767, 23453, 23416, 24608, 23767), listOf(24608, 28360))),
    Constellation("Boötes", listOf(listOf(71795, 69673, 67927, 67275), listOf(69673, 72105, 74666, 73555, 71075, 71053, 69673), listOf(71075, 69732, 69483, 70497, 69732))),
    Constellation("Caelum", listOf(listOf(23595, 21861, 21770, 21060))),
    Constellation("Camelopardalis", listOf(listOf(23522, 22783, 17959))),
    Constellation("Cancer", listOf(listOf(44066, 42911, 40526), listOf(42911, 42806, 43103))),
    Constellation("Canes Venatici", listOf(listOf(63125, 61317))),
    Constellation("Canis Major", listOf(listOf(35904, 34444, 33579, 33152, 31592, 30324, 32349, 34444), listOf(32349, 33347, 34045, 33160, 33347))),
    Constellation("Canis Minor", listOf(listOf(37279, 36188))),
    Constellation("Capricornus", listOf(listOf(100027, 100345, 102485, 102978, 105881, 106723, 107556, 106985, 104139, 100027))),
    Constellation("Cassiopeia", listOf(listOf(8886, 6686, 4427, 3179, 746)))
)

private fun star(hipDesignation: Int): Star {
    return STAR_CATALOG_BY_HIP[hipDesignation]!!
}