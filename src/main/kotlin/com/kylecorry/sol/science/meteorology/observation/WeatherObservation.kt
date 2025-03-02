package com.kylecorry.sol.science.meteorology.observation

import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Quantity
import com.kylecorry.sol.units.Reading
import com.kylecorry.sol.units.Speed
import java.time.Instant

sealed class WeatherObservation<T>(
    val time: Instant,
    val value: T
) {
    class Pressure(time: Instant, value: Quantity<com.kylecorry.sol.units.Pressure>) : WeatherObservation<Quantity<com.kylecorry.sol.units.Pressure>>(time, value)
    class WindSpeed(time: Instant, value: Speed) : WeatherObservation<Speed>(time, value)

    /**
     * A wind direction observation - the direction the wind is coming from.
     */
    class WindDirection(time: Instant, value: Bearing) : WeatherObservation<Bearing>(time, value)
    class CloudGenus(time: Instant, value: com.kylecorry.sol.science.meteorology.clouds.CloudGenus?) : WeatherObservation<com.kylecorry.sol.science.meteorology.clouds.CloudGenus?>(time, value)

    fun asReading(): Reading<T> {
        return Reading(value, time)
    }

}