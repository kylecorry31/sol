package com.kylecorry.trailsensecore.domain.units

data class Distance(val distance: Float, val units: DistanceUnits) {

    private val unitService = UnitService()

    fun convertTo(newUnits: DistanceUnits): Distance {
        val newDistance = unitService.convert(distance, units, newUnits)
        return Distance(newDistance, newUnits)
    }

    fun meters(): Distance {
        return convertTo(DistanceUnits.Meters)
    }

    companion object {

        fun meters(distance: Float): Distance {
            return Distance(distance, DistanceUnits.Meters)
        }

        fun feet(distance: Float): Distance {
            return Distance(distance, DistanceUnits.Feet)
        }

        fun centimeters(distance: Float): Distance {
            return Distance(distance, DistanceUnits.Centimeters)
        }

        fun kilometers(distance: Float): Distance {
            return Distance(distance, DistanceUnits.Kilometers)
        }

        fun miles(distance: Float): Distance {
            return Distance(distance, DistanceUnits.Miles)
        }

        fun nauticalMiles(distance: Float): Distance {
            return Distance(distance, DistanceUnits.NauticalMiles)
        }

        fun yards(distance: Float): Distance {
            return Distance(distance, DistanceUnits.Yards)
        }

    }

}