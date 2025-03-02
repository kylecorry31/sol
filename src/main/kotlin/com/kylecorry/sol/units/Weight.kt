package com.kylecorry.sol.units

enum class Weight(override val id: Int, override val multiplierToBase: Float) : PhysicalUnit {
    Pounds(1, 453.592f),
    Ounces(2, 28.3495f),
    Kilograms(3, 1000f),
    Grams(4, 1f);

    companion object {
        fun grams(weight: Float): Quantity<Weight> {
            return Quantity(weight, Grams)
        }
    }
}

fun Quantity<Weight>.grams(): Quantity<Weight> {
    return convertTo(Weight.Grams)
}