package com.kylecorry.sol.units

@JvmInline
value class Speed private constructor(private val measure: Measure) {
    val speed: Float
        get() = measureValue(measure)
    val distanceUnits: DistanceUnits
        get() = measureUnit1<DistanceUnits>(measure)
    val timeUnits: TimeUnits
        get() = measureUnit2<TimeUnits>(measure)

    fun convertTo(newDistanceUnits: DistanceUnits, newTimeUnits: TimeUnits): Speed {
        val distance = Distance.from(speed, distanceUnits).convertTo(newDistanceUnits).value
        val newSpeed = (distance / timeUnits.seconds) * newTimeUnits.seconds
        return from(newSpeed.toFloat(), newDistanceUnits, newTimeUnits)
    }

    override fun toString(): String {
        return "$speed $distanceUnits / $timeUnits"
    }

    companion object {
        fun from(value: Float, distanceUnits: DistanceUnits, timeUnits: TimeUnits): Speed {
            return Speed(packMeasureMultiUnit(value, distanceUnits, timeUnits))
        }
    }

}
