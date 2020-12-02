package com.kylecorry.trailsensecore.domain.weather

import java.time.Instant

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
     * Calculates the distance of the lightning strike from the current position in meters
     * @param lightning The time the lightning was seen
     * @param thunder The time the thunder was heard
     * @return The distance to the lightning strike in meters
     */
    fun getLightningStrikeDistance(lightning: Instant, thunder: Instant): Float
}