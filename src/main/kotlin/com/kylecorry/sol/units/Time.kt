package com.kylecorry.sol.units

enum class Time(override val id: Int, override val multiplierToBase: Float) : PhysicalUnit {
    Milliseconds(1, 1 / 1000f),
    Seconds(2, 1f),
    Minutes(3, 60f),
    Hours(4, 3600f),
    Days(5, 86400f);

    companion object {
        fun seconds(time: Float): Quantity<Time> {
            return Quantity(time, Seconds)
        }
    }
}

fun Quantity<Time>.seconds(): Quantity<Time> {
    return convertTo(Time.Seconds)
}