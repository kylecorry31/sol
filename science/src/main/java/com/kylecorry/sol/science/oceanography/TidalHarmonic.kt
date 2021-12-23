package com.kylecorry.sol.science.oceanography

data class TidalHarmonic(val constituent: TideConstituent, val amplitude: Float, val phase: Float)

enum class TideConstituent(val id: Long, val speed: Float) {
    M2(1, 28.984104f),
    S2(2, 30f),
    N2(3, 28.43973f),
    K1(4, 15.041069f),
    M4(5, 57.96821f),
    O1(6, 13.943035f),
    P1(30, 14.958931f),
    L2(33, 29.528479f),
    K2(35, 30.082138f),
    MS4(37, 58.984104f),
    Z0(0, 0f)
}