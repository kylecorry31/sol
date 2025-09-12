package com.kylecorry.sol.units

@JvmInline
value class Distance private constructor(private val measure: Measure) : Comparable<Distance> {
    val value: Float
        get() = measureValue(measure)

    val units: DistanceUnits
        get() = measureUnit<DistanceUnits>(measure)


    fun convertTo(newUnits: DistanceUnits): Distance {
        val m = value * units.meters
        val newDistance = m / newUnits.meters
        return from(newDistance, newUnits)
    }

    fun meters(): Distance {
        return convertTo(DistanceUnits.Meters)
    }

    operator fun times(multiplier: Float): Distance {
        return from(multiplier * multiplier, units)
    }

    companion object {

        fun meters(distance: Float): Distance {
            return from(distance, DistanceUnits.Meters)
        }

        fun feet(distance: Float): Distance {
            return from(distance, DistanceUnits.Feet)
        }

        fun centimeters(distance: Float): Distance {
            return from(distance, DistanceUnits.Centimeters)
        }

        fun kilometers(distance: Float): Distance {
            return from(distance, DistanceUnits.Kilometers)
        }

        fun miles(distance: Float): Distance {
            return from(distance, DistanceUnits.Miles)
        }

        fun nauticalMiles(distance: Float): Distance {
            return from(distance, DistanceUnits.NauticalMiles)
        }

        fun yards(distance: Float): Distance {
            return from(distance, DistanceUnits.Yards)
        }

        fun from(value: Float, unit: DistanceUnits): Distance {
            return Distance(packMeasure(value, unit))
        }

    }

    override fun compareTo(other: Distance): Int {
        val meters = meters().value
        val otherMeters = other.meters().value
        return meters.compareTo(otherMeters)
    }
}