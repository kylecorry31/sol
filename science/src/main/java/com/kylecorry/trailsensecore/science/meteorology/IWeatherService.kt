package com.kylecorry.trailsensecore.science.meteorology

import com.kylecorry.trailsensecore.units.Distance
import com.kylecorry.trailsensecore.units.Pressure
import com.kylecorry.trailsensecore.units.Temperature
import com.kylecorry.trailsensecore.science.shared.ISeasonService
import com.kylecorry.trailsensecore.science.meteorology.clouds.ICloudService
import java.time.Duration
import java.time.Instant

interface IWeatherService : ICloudService, ISeasonService {

    fun getSeaLevelPressure(
        pressure: Pressure,
        altitude: Distance,
        temperature: Temperature? = null
    ): Pressure

    fun isHighPressure(pressure: Pressure): Boolean

    fun isLowPressure(pressure: Pressure): Boolean

    /**
     * Calculates the tendency
     * @param last The last pressure reading (hPa)
     * @param current The current pressure reading (hPa)
     * @param duration The duration between the last and current pressure reading
     * @param changeThreshold The change threshold (hPa / 3 hr)
     * @return The pressure tendency (hPa / 3 hr)
     */
    fun getTendency(
        last: Pressure,
        current: Pressure,
        duration: Duration,
        changeThreshold: Float
    ): PressureTendency

    /**
     * Forecast the weather in the next few hours
     * @param tendency The current pressure tendency (hPa / 3 hr)
     * @param stormThreshold (optional) The change threshold to consider a storm (hPa / 3 hr)
     * @return The predicted weather
     */
    fun forecast(
        tendency: PressureTendency,
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

    /**
     * Determines if the lightning strike is close enough for concern
     * @param distance The distance to the lightning strike
     * @return true if the strike is dangerous, false otherwise
     */
    fun isLightningStrikeDangerous(distance: Distance): Boolean

    /**
     * Calculates the ambient temperature from sequential temperature readings
     * @param temp0 the initial temperature (celsius)
     * @param temp1 the temperature occurring 1 time unit after temp0
     * @param temp2 the temperature occurring 2 time units after temp0
     * @return the ambient temperature in celsius or null if the readings weren't all increasing or decreasing
     */
    fun getAmbientTemperature(temp0: Float, temp1: Float, temp2: Float): Float?

    /**
     * Calculates the temperature at an elevation
     * @param temperature the temperature at the base elevation
     * @param baseElevation the elevation in which the temperature reading was taken
     * @param destElevation the elevation of the destination
     * @return the temperature at the destination
     */
    fun getTemperatureAtElevation(
        temperature: Temperature,
        baseElevation: Distance,
        destElevation: Distance
    ): Temperature
}