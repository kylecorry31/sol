package com.kylecorry.sol.science.astronomy.stars

enum class Constellation(val edges: List<Pair<Star, Star>>) {
    BigDipper(
        listOf(
            star("Alkaid") to star("Mizar"),
            star("Mizar") to star("Alioth"),
            star("Megrez") to star("Dubhe"),
            star("Dubhe") to star("Merak"),
            star("Merak") to star("Phecda"),
            star("Phecda") to star("Megrez")
        )
    ),
    SouthernCross(
        listOf(
            star("Acrux") to star("Gacrux"),
            star("Mimosa") to star("Imai")
        )
    ),
    Orion(
        listOf(
            star("Rigel") to star("Mintaka"),
            star("Mintaka") to star("Alnilam"),
            star("Alnilam") to star("Alnitak"),
            star("Alnitak") to star("Saiph"),
            star("Alnitak") to star("Betelgeuse"),
            star("Betelgeuse") to star("Bellatrix"),
            star("Bellatrix") to star("Mintaka"),
            star("Bellatrix") to star("Meissa"),
            star("Betelgeuse") to star("Meissa")
        )
    ),
    Cassiopeia(
        listOf(
            star("Caph") to star("Schedar"),
            star("Schedar") to star("Gamma Cassiopeiae"),
            star("Gamma Cassiopeiae") to star("Ruchbah"),
            star("Ruchbah") to star("Segin")
        )
    ),
}

private fun star(name: String): Star {
    return STAR_CATALOG.first { it.name == name }
}