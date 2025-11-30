package com.kylecorry.sol.units

enum class TimeUnits(val id: Int, val seconds: Double) {
    Milliseconds(1, 1 / 1000.0),
    Seconds(2, 1.0),
    Minutes(3, 60.0),
    Hours(4, 3600.0),
    Days(5, 86400.0)
}