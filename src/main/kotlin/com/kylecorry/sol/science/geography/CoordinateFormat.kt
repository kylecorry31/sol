package com.kylecorry.sol.science.geography

enum class CoordinateFormat(val id: Int) {
    DecimalDegrees(1),
    DegreesDecimalMinutes(2),
    DegreesMinutesSeconds(3),
    UTM(4),
    MGRS(5),
    USNG(6),
    OSGB(7)
}