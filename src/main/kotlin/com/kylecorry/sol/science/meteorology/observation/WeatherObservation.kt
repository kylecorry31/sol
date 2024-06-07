package com.kylecorry.sol.science.meteorology.observation

import com.kylecorry.sol.science.meteorology.clouds.CloudGenus
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.Reading
import com.kylecorry.sol.units.Speed
import java.time.Instant

sealed class WeatherObservation<T>(
    val time: Instant,
    val value: T
) {
    class PressureObservation(time: Instant, value: Pressure) : WeatherObservation<Pressure>(time, value)
    class WindSpeedObservation(time: Instant, value: Speed) : WeatherObservation<Speed>(time, value)
    class WindDirectionObservation(time: Instant, value: Bearing) : WeatherObservation<Bearing>(time, value)
    class CloudGenusObservation(time: Instant, value: CloudGenus?) : WeatherObservation<CloudGenus?>(time, value)

    fun asReading(): Reading<T> {
        return Reading(value, time)
    }

}