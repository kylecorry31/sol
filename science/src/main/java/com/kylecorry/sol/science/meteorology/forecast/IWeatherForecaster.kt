package com.kylecorry.sol.science.meteorology.forecast

import com.kylecorry.sol.units.Reading

interface IWeatherForecaster<T> {

    /**
     * Determines how likely the weather is going to worsen or improve
     * -1 = storm
     * < 0 = worsening
     * 0 = no change
     * > 0 = improving
     * 1 = good weather
     */
    fun forecast(readings: List<Reading<T>>): Float

}