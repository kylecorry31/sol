package com.kylecorry.sol.units

data class Speed(val speed: Float, val distanceUnits: Distance, val timeUnits: TimeUnits) {

    fun convertTo(newDistance: Distance, newTimeUnits: TimeUnits): Speed {
        val distance = Quantity(speed, distanceUnits).convertTo(newDistance).amount
        val newSpeed = (distance / timeUnits.seconds) * newTimeUnits.seconds
        return Speed(newSpeed, newDistance, newTimeUnits)
    }

}
