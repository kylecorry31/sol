package com.kylecorry.sol.science.meteorology.forecast

import com.kylecorry.sol.units.Reading

interface IWeatherForecaster<T> {

    fun forecast(readings: List<Reading<T>>): WeatherForecast

}