package com.kylecorry.sol.science.astronomy.stars

enum class Constellation(val edges: List<Pair<Star, Star>>) {
    BigDipper(
        listOf(
            Star.Alkaid to Star.Mizar,
            Star.Mizar to Star.Alioth,
            Star.Megrez to Star.Dubhe,
            Star.Dubhe to Star.Merak,
            Star.Merak to Star.Phecda,
            Star.Phecda to Star.Megrez
        )
    ),
    SouthernCross(
        listOf(
            Star.Acrux to Star.Gacrux,
            Star.Mimosa to Star.Imai
        )
    ),
    Orion(
        listOf(
            Star.Rigel to Star.Mintaka,
            Star.Mintaka to Star.Alnilam,
            Star.Alnilam to Star.Alnitak,
            Star.Alnitak to Star.Saiph,
            Star.Alnitak to Star.Betelgeuse,
            Star.Betelgeuse to Star.Bellatrix,
            Star.Bellatrix to Star.Mintaka,
            Star.Bellatrix to Star.Meissa,
            Star.Betelgeuse to Star.Meissa
        )
    )
}