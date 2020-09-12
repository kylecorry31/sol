package com.kylecorry.trailsensecore.domain.units

class UnitService {

    fun convert(pressure: Float, from: PressureUnits, to: PressureUnits): Float {
        val hpa = when (from) {
            PressureUnits.Hpa -> pressure
            PressureUnits.Mbar -> pressure
            PressureUnits.Inhg -> pressure / 0.02953f
            PressureUnits.Psi -> pressure / 0.01450f
        }

        return when (to) {
            PressureUnits.Hpa -> hpa
            PressureUnits.Inhg -> 0.02953f * hpa
            PressureUnits.Mbar -> hpa
            PressureUnits.Psi -> 0.01450f * hpa
        }
    }

    fun convert(temperature: Float, from: TemperatureUnits, to: TemperatureUnits): Float {
        val c = when (from) {
            TemperatureUnits.C -> temperature
            TemperatureUnits.F -> (temperature - 32) * 5 / 9
        }

        return when (to) {
            TemperatureUnits.C -> c
            TemperatureUnits.F -> (c * 9 / 5) + 32
        }
    }

    fun convert(distance: Float, from: DistanceUnits, to: DistanceUnits): Float {
        val m = when (from) {
            DistanceUnits.Meters -> distance
            DistanceUnits.Kilometers -> distance * 1000
            DistanceUnits.Miles -> distance * 5280f / 3.28084f
            DistanceUnits.Feet -> distance / 3.28084f
        }

        return when (to) {
            DistanceUnits.Meters -> m
            DistanceUnits.Kilometers -> m / 1000f
            DistanceUnits.Feet -> m * 3.28084f
            DistanceUnits.Miles -> (m * 3.28084f) / 5280f
        }
    }


}