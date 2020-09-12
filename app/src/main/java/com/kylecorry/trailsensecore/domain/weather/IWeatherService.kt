package com.kylecorry.trailsensecore.domain.weather

import com.kylecorry.trail_sense.weather.domain.HeatAlert
import com.kylecorry.trail_sense.weather.domain.HumidityComfortLevel

interface IWeatherService {

    /**
     * Calculates the tendency
     * @param last The last pressure reading (hPa)
     * @param current The current pressure reading (hPa)
     * @param changeThreshold The change threshold (hPa / 3 hr)
     * @return The pressure tendency (hPa / 3 hr)
     */
    fun getTendency(
        last: PressureReading,
        current: PressureReading,
        changeThreshold: Float
    ): PressureTendency

    /**
     * Forecast the weather in the next few hours
     * @param tendency The current pressure tendency (hPa / 3 hr)
     * @param currentPressure The current pressure - ideally adjusted for sea level (hPa)
     * @param stormThreshold (optional) The change threshold to consider a storm (hPa / 3 hr)
     * @return The predicted weather
     */
    fun forecast(
        tendency: PressureTendency,
        currentPressure: PressureReading,
        stormThreshold: Float? = null
    ): Weather

    /**
     * Convert a reading to sea level
     * @param reading The pressure altitude reading (hPa, m)
     * @param temperature (optional) The temperature (C)
     * @return The reading at sea level pressure (hPa)
     */
    fun convertToSeaLevel(
        reading: PressureAltitudeReading,
        temperature: Float? = null
    ): PressureReading

    /**
     * Determines if a pressure is high or low
     * @param reading The pressure - ideally adjusted for sea level (hPa)
     * @return The pressure classification
     */
    fun classifyPressure(reading: PressureReading): PressureClassification

    /**
     * Calculates the heat index
     * @param temperature The temperature (C)
     * @param relativeHumidity The relative humidity (%)
     * @return The heat index (C)
     */
    fun getHeatIndex(temperature: Float, relativeHumidity: Float): Float

    /**
     * Get the current heat alert level
     * @param heatIndex The heat index (C)
     * @return The heat alert level
     */
    fun getHeatAlert(heatIndex: Float): HeatAlert

    /**
     * Calculates the dew point
     * @param temperature The temperature (C)
     * @param relativeHumidity The relative humidity (%)
     * @return The dew point (C)
     */
    fun getDewPoint(temperature: Float, relativeHumidity: Float): Float

    /**
     * Gets the humidity comfort level
     * @param dewPoint The dew point (C)
     * @return The humidity comfort level
     */
    fun getHumidityComfortLevel(dewPoint: Float): HumidityComfortLevel
}