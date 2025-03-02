package com.kylecorry.sol.units

enum class Pressure(override val id: Int, override val multiplierToBase: Float) : PhysicalUnit {
    Hpa(1, 1f),
    Mbar(2, 1f),
    Inhg(3, 1 / 0.02953f),
    Psi(4, 1 / 0.014503774f),
    MmHg(5, 1.3332239f);

    companion object {
        fun hpa(pressure: Float): Quantity<Pressure> {
            return Quantity(pressure, Hpa)
        }
    }
}

fun Quantity<Pressure>.hpa(): Quantity<Pressure> {
    return convertTo(Pressure.Hpa)
}