package com.kylecorry.sol.science.meteorology

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.meteorology.clouds.CloudGenus
import com.kylecorry.sol.science.meteorology.clouds.ICloudService
import com.kylecorry.sol.science.meteorology.forecast.ForecastSource
import com.kylecorry.sol.science.meteorology.observation.WeatherObservation
import com.kylecorry.sol.science.shared.ISeasonService
import com.kylecorry.sol.units.*
import java.time.Duration
import java.time.Instant

interface IWeatherService : ICloudService, ISeasonService {

    fun getSeaLevelPressure(
        pressure: Quantity<Pressure>,
        altitude: Quantity<Distance>,
        temperature: Quantity<Temperature>? = null
    ): Quantity<Pressure>

    fun isHighPressure(pressure: Quantity<Pressure>): Boolean

    fun isLowPressure(pressure: Quantity<Pressure>): Boolean

    /**
     * Calculates the tendency
     * @param last The last pressure reading (hPa)
     * @param current The current pressure reading (hPa)
     * @param duration The duration between the last and current pressure reading
     * @param changeThreshold The change threshold (hPa / hr)
     * @return The pressure tendency (hPa / hr)
     */
    fun getTendency(
        last: Quantity<Pressure>,
        current: Quantity<Pressure>,
        duration: Duration,
        changeThreshold: Float
    ): PressureTendency

    /**
     * Forecast the weather in the next few hours
     * @param tendency The current pressure tendency (hPa / hr)
     * @param stormThreshold (optional) The change threshold to consider a storm (hPa / hr)
     * @return The predicted weather
     */
    fun forecast(
        tendency: PressureTendency,
        stormThreshold: Float? = null
    ): Weather

    /**
     * Forecast the weather for the next few hours
     * @param pressures the pressure readings
     * @param clouds the cloud readings, null cloud genus = Clear
     * @param dailyTemperatureRange the daily temperature range for the given time (the next 24 hours, but can be less than that)
     * @param pressureChangeThreshold (optional) the change threshold for pressure to be considered changing (hPa / hr)
     * @param pressureStormChangeThreshold (optional) the change threshold for pressure to be considered a storm (hPa / hr)
     * @param time the time to calculate the forecast after
     * @param location the location to calculate the forecast for (may be used to determine climate zone / hemisphere - does not need to be very accurate)
     * @param source the source to use to derive the forecast
     * @return the predicted weather (now and later - times are not accurate yet)
     */
    fun forecast(
        pressures: List<Reading<Quantity<Pressure>>>,
        clouds: List<Reading<CloudGenus?>>,
        dailyTemperatureRange: Range<Quantity<Temperature>>?,
        pressureChangeThreshold: Float = 0.5f,
        pressureStormChangeThreshold: Float = 2f,
        time: Instant = Instant.now(),
        location: Coordinate = Coordinate.zero,
        source: ForecastSource = ForecastSource.Sol
    ): List<WeatherForecast>

    /**
     * Forecast the weather for the next few hours
     * @param observations the weather observations
     * @param dailyTemperatureRange the daily temperature range for the given time (the next 24 hours, but can be less than that)
     * @param pressureChangeThreshold (optional) the change threshold for pressure to be considered changing (hPa / hr)
     * @param pressureStormChangeThreshold (optional) the change threshold for pressure to be considered a storm (hPa / hr)
     * @param time the time to calculate the forecast after
     * @param location the location to calculate the forecast for (may be used to determine climate zone / hemisphere - does not need to be very accurate)
     * @param source the source to use to derive the forecast
     * @return the predicted weather (now and later - times are not accurate yet)
     */
    fun forecast(
        observations: List<WeatherObservation<*>>,
        dailyTemperatureRange: Range<Quantity<Temperature>>?,
        pressureChangeThreshold: Float = 0.5f,
        pressureStormChangeThreshold: Float = 2f,
        time: Instant = Instant.now(),
        location: Coordinate = Coordinate.zero,
        source: ForecastSource = ForecastSource.Sol
    ): List<WeatherForecast>

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
    fun isLightningStrikeDangerous(distance: Quantity<Distance>): Boolean

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
        temperature: Quantity<Temperature>,
        baseElevation: Quantity<Distance>,
        destElevation: Quantity<Distance>
    ): Quantity<Temperature>
}