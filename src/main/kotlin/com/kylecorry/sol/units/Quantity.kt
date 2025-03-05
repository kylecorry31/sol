package com.kylecorry.sol.units

interface Unit {
    val id: Int
    val multiplierToBase: Float
    val offsetToBase: Float
        get() = 0.0f
}

data class Quantity<U : Unit>(val amount: Float, val units: U): Comparable<Quantity<U>> {
    fun convertTo(to: U): Quantity<U> {
        val amountInBase = (amount + units.offsetToBase) * units.multiplierToBase
        val amountInNew = amountInBase / to.multiplierToBase - to.offsetToBase
        return Quantity(amountInNew, to)
    }

    operator fun times(scalar: Float): Quantity<U> {
        return Quantity(amount * scalar, units)
    }

    operator fun div(scalar: Float): Quantity<U> {
        return Quantity(amount / scalar, units)
    }

    operator fun plus(other: Quantity<U>): Quantity<U> {
        return Quantity(amount + other.convertTo(units).amount, units)
    }

    operator fun minus(other: Quantity<U>): Quantity<U> {
        return Quantity(amount - other.convertTo(units).amount, units)
    }

    override fun compareTo(other: Quantity<U>): Int {
        return amount.compareTo(other.convertTo(units).amount)
    }
}