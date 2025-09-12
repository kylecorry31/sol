package com.kylecorry.sol.units

data class Speed(val speed: Float, val distanceUnits: DistanceUnits, val timeUnits: TimeUnits){

    fun convertTo(newDistanceUnits: DistanceUnits, newTimeUnits: TimeUnits): Speed {
        val distance = Distance.from(speed, distanceUnits).convertTo(newDistanceUnits).value
        val newSpeed = (distance / timeUnits.seconds) * newTimeUnits.seconds
        return Speed(newSpeed, newDistanceUnits, newTimeUnits)
    }

}
