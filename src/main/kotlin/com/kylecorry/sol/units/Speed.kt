package com.kylecorry.sol.units

data class Speed(val speed: Float, val distanceUnits: Distance, val timeUnits: Time) {

    fun convertTo(newDistance: Distance, newTimeUnits: Time): Speed {
        val distance = Quantity(speed, distanceUnits).convertTo(newDistance).amount
        val newSpeed = (distance / timeUnits.multiplierToBase) * newTimeUnits.multiplierToBase
        return Speed(newSpeed, newDistance, newTimeUnits)
    }

}
