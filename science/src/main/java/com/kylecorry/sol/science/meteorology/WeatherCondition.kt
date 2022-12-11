package com.kylecorry.sol.science.meteorology

enum class WeatherCondition(val id: Long) {
    Clear(1),
    Overcast(2),
    Precipitation(3),
    Storm(4),
    Wind(5),
    Rain(6),
    Snow(7)
}