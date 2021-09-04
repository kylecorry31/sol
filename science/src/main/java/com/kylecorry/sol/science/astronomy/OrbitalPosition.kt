package com.kylecorry.sol.science.astronomy

internal enum class OrbitalPosition(val solarLongitude: Int) {
    WinterSolstice(270),
    VernalEquinox(0),
    SummerSolstice(90),
    AutumnalEquinox(180)
}