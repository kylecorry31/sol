package com.kylecorry.sol.science.meteorology.forecast

data class WeatherForecast(val weather: Weather, val confidence: Float) {
    val isBadWeather =
        weather == Weather.WorseningSlow || weather == Weather.WorseningFast || weather == Weather.Storm
    val isGoodWeather = weather == Weather.ImprovingSlow || weather == Weather.ImprovingFast
}
