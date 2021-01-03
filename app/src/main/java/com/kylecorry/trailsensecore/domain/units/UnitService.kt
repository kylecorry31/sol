package com.kylecorry.trailsensecore.domain.units

class UnitService {

    /**
     * Converts a pressure from one unit to another
     * @param pressure The pressure in the from units
     * @param from The pressure to convert from
     * @param to The pressure to convert to
     * @return The pressure in the to units
     */
    fun convert(pressure: Float, from: PressureUnits, to: PressureUnits): Float {
        val hpa = when (from) {
            PressureUnits.Hpa -> pressure
            PressureUnits.Mbar -> pressure
            PressureUnits.Inhg -> pressure / 0.02953f
            PressureUnits.Psi -> pressure / 0.0145037738f
        }

        return when (to) {
            PressureUnits.Hpa -> hpa
            PressureUnits.Inhg -> 0.02953f * hpa
            PressureUnits.Mbar -> hpa
            PressureUnits.Psi -> 0.0145037738f * hpa
        }
    }

    /**
     * Converts a temperature from one unit to another
     * @param temperature The temperature in the from units
     * @param from The temperature to convert from
     * @param to The temperature to convert to
     * @return The temperature in the to units
     */
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

    /**
     * Converts a distance from one unit to another
     * @param distance The distance in the from units
     * @param from The distance to convert from
     * @param to The distance to convert to
     * @return The distance in the to units
     */
    fun convert(distance: Float, from: DistanceUnits, to: DistanceUnits): Float {
        val m = distance * from.meters
        return m / to.meters
    }

}