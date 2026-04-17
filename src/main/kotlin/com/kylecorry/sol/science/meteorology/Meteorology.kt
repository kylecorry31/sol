package com.kylecorry.sol.science.meteorology

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.meteorology.clouds.CloudCover
import com.kylecorry.sol.science.meteorology.clouds.CloudGenus
import com.kylecorry.sol.science.meteorology.clouds.CloudLevel
import com.kylecorry.sol.science.meteorology.clouds.CloudService
import com.kylecorry.sol.science.meteorology.forecast.ForecastSource
import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecaster
import com.kylecorry.sol.science.meteorology.forecast.ZambrettiForecaster
import com.kylecorry.sol.science.meteorology.observation.WeatherObservation
import com.kylecorry.sol.science.shared.Season
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.DistanceUnits
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.Reading
import com.kylecorry.sol.units.Speed
import com.kylecorry.sol.units.Temperature
import com.kylecorry.sol.units.TemperatureUnits
import com.kylecorry.sol.units.TimeUnits
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

object Meteorology {

    private val cloudService = CloudService()

    fun getAltitude(pressure: Pressure, seaLevelPressure: Pressure): Distance {
        // TODO: Factor in temperature
        val hpa = pressure.hpa().value
        val seaHpa = seaLevelPressure.hpa().value
        val meters = 44330.0 * (1 - (hpa / seaHpa).toDouble().pow(1 / 5.255))
        return Distance.meters(meters.toFloat())
    }

    fun getSeaLevelPressure(
        pressure: Pressure, altitude: Distance, temperature: Temperature? = null
    ): Pressure {
        val hpa = pressure.hpa().value
        val meters = altitude.meters().value
        val celsius = temperature?.celsius()?.value
        val adjustedPressure = if (celsius != null) {
            hpa * (1 - ((0.0065f * meters) / (celsius + 0.0065f * meters + 273.15f))).pow(
                -5.257f
            )
        } else {
            hpa * (1 - meters / 44330.0).pow(-5.255).toFloat()
        }

        return Pressure.hpa(adjustedPressure).convertTo(pressure.units)
    }

    fun isHighPressure(pressure: Pressure): Boolean {
        return pressure.hpa().value >= 1022.689
    }

    fun isLowPressure(pressure: Pressure): Boolean {
        return pressure.hpa().value <= 1009.144
    }

    /**
     * Calculates the tendency
     * @param last The last pressure reading (hPa)
     * @param current The current pressure reading (hPa)
     * @param duration The duration between the last and current pressure reading
     * @param changeThreshold The change threshold (hPa / hr)
     * @return The pressure tendency (hPa / hr)
     */
    fun getTendency(
        last: Pressure, current: Pressure, duration: Duration, changeThreshold: Float
    ): PressureTendency {
        val diff = current.hpa().value - last.hpa().value
        val dt = duration.seconds

        if (dt == 0L) {
            return PressureTendency(PressureCharacteristic.Steady, 0f)
        }

        val changeAmt = (diff / dt) * 60 * 60

        val fastThreshold = changeThreshold + 2 / 3f

        val characteristic = when {
            changeAmt <= -fastThreshold -> PressureCharacteristic.FallingFast
            changeAmt <= -changeThreshold -> PressureCharacteristic.Falling
            changeAmt >= fastThreshold -> PressureCharacteristic.RisingFast
            changeAmt >= changeThreshold -> PressureCharacteristic.Rising
            else -> PressureCharacteristic.Steady
        }

        return PressureTendency(characteristic, changeAmt)

    }

    /**
     * Forecast the weather in the next few hours
     * @param tendency The current pressure tendency (hPa / hr)
     * @param stormThreshold (optional) The change threshold to consider a storm (hPa / hr)
     * @return The predicted weather
     */
    fun forecast(
        tendency: PressureTendency, stormThreshold: Float? = null
    ): Weather {
        val isStorm = tendency.amount <= (stormThreshold ?: -2f)

        if (isStorm) {
            return Weather.Storm
        }

        return when (tendency.characteristic) {
            PressureCharacteristic.FallingFast -> Weather.WorseningFast
            PressureCharacteristic.Falling -> Weather.WorseningSlow
            PressureCharacteristic.RisingFast -> Weather.ImprovingFast
            PressureCharacteristic.Rising -> Weather.ImprovingSlow
            else -> Weather.NoChange
        }
    }

    /**
     * Forecast the weather for the next few hours
     * @param pressures the pressure readings
     * @param clouds the cloud readings, null cloud genus = Clear
     * @param dailyTemperatureRange The daily temperature range for the given time
     *   (the next 24 hours, but can be less than that)
     * @param pressureChangeThreshold (Optional) The change threshold for pressure to be
     *   considered changing (hPa / hr)
     * @param pressureStormChangeThreshold (Optional) The change threshold for pressure
     *   to be considered a storm (hPa / hr)
     * @param time the time to calculate the forecast after
     * @param location The location to calculate the forecast for
     *   (may be used to determine climate zone / hemisphere and does not need to be very accurate)
     * @param source the source to use to derive the forecast
     * @return the predicted weather (now and later - times are not accurate yet)
     */
    fun forecast(
        pressures: List<Reading<Pressure>>,
        clouds: List<Reading<CloudGenus?>>,
        dailyTemperatureRange: Range<Temperature>?,
        pressureChangeThreshold: Float = 0.5f,
        pressureStormChangeThreshold: Float = 2f,
        time: Instant = Instant.now(),
        location: Coordinate = Coordinate.zero,
        source: ForecastSource = ForecastSource.Sol
    ): List<WeatherForecast> {
        val pressureObservations = pressures.map { WeatherObservation.Pressure(it.time, it.value) }
        val cloudObservations = clouds.map { WeatherObservation.CloudGenus(it.time, it.value) }
        return forecast(
            pressureObservations + cloudObservations,
            dailyTemperatureRange,
            pressureChangeThreshold,
            pressureStormChangeThreshold,
            time,
            location,
            source
        )
    }

    /**
     * Forecast the weather for the next few hours
     * @param observations the weather observations
     * @param dailyTemperatureRange The daily temperature range for the given time
     *   (the next 24 hours, but can be less than that)
     * @param pressureChangeThreshold (Optional) The change threshold for pressure to be
     *   considered changing (hPa / hr)
     * @param pressureStormChangeThreshold (Optional) The change threshold for pressure
     *   to be considered a storm (hPa / hr)
     * @param time the time to calculate the forecast after
     * @param location The location to calculate the forecast for
     *   (may be used to determine climate zone / hemisphere and does not need to be very accurate)
     * @param source the source to use to derive the forecast
     * @return the predicted weather (now and later - times are not accurate yet)
     */
    fun forecast(
        observations: List<WeatherObservation<*>>,
        dailyTemperatureRange: Range<Temperature>?,
        pressureChangeThreshold: Float = 0.5f,
        pressureStormChangeThreshold: Float = 2f,
        time: Instant = Instant.now(),
        location: Coordinate = Coordinate.zero,
        source: ForecastSource = ForecastSource.Sol
    ): List<WeatherForecast> {

        val forecaster = when (source) {
            ForecastSource.Zambretti -> ZambrettiForecaster
            ForecastSource.Sol -> SolForecaster
        }

        return forecaster.forecast(
            observations.sortedBy { it.time },
            dailyTemperatureRange,
            time,
            pressureChangeThreshold,
            pressureStormChangeThreshold,
            location
        )
    }

    /**
     * Calculates the heat index
     * @param temperature The temperature (C)
     * @param relativeHumidity The relative humidity (%)
     * @return The heat index (C)
     */
    fun getHeatIndex(temperature: Float, relativeHumidity: Float): Float {
        if (temperature < 27) return temperature

        val c1 = -8.78469475556
        val c2 = 1.61139411
        val c3 = 2.33854883889
        val c4 = -0.14611605
        val c5 = -0.012308094
        val c6 = -0.0164248277778
        val c7 = 0.002211732
        val c8 = 0.00072546
        val c9 = -0.000003582

        val hi =
            c1 +
                c2 * temperature +
                c3 * relativeHumidity +
                c4 * temperature * relativeHumidity +
                c5 * temperature * temperature +
                c6 * relativeHumidity * relativeHumidity +
                c7 * temperature * temperature * relativeHumidity +
                c8 * temperature * relativeHumidity * relativeHumidity +
                c9 * temperature * temperature * relativeHumidity * relativeHumidity

        return hi.toFloat()
    }

    /**
     * Get the current heat alert level
     * @param heatIndex The heat index (C)
     * @return The heat alert level
     */
    fun getHeatAlert(heatIndex: Float): HeatAlert {
        return when {
            heatIndex <= -25 -> HeatAlert.FrostbiteDanger
            heatIndex <= -17 -> HeatAlert.FrostbiteWarning
            heatIndex <= 5 -> HeatAlert.FrostbiteCaution
            heatIndex < 27 -> HeatAlert.Normal
            heatIndex <= 32.5 -> HeatAlert.HeatCaution
            heatIndex <= 39 -> HeatAlert.HeatWarning
            heatIndex <= 50 -> HeatAlert.HeatAlert
            else -> HeatAlert.HeatDanger
        }
    }

    /**
     * Get the wind chill (https://www.weather.gov/safety/cold-wind-chill-chart)
     * @param temperature the air temperature
     * @param windSpeed the wind speed
     * @return the wind chill temperature
     */
    fun getWindChill(temperature: Temperature, windSpeed: Speed): Temperature {
        val t = temperature.convertTo(TemperatureUnits.Fahrenheit).value.toDouble()
        val v = windSpeed.convertTo(DistanceUnits.Miles, TimeUnits.Hours).speed.toDouble()

        // Wind chill is defined only at or below 50F and speeds above 3mph
        if (t > 50f || v <= 3) {
            return temperature
        }

        val vPow = v.pow(0.16)
        val chill = 35.74 + 0.6215 * t - 35.75 * vPow + 0.4275 * t * vPow
        return Temperature.fahrenheit(chill.toFloat()).convertTo(temperature.units)
    }

    /**
     * Calculates the dew point
     * @param temperature The temperature (C)
     * @param relativeHumidity The relative humidity (%)
     * @return The dew point (C)
     */
    fun getDewPoint(temperature: Float, relativeHumidity: Float): Float {
        val m = 17.62
        val tn = 243.12
        var lnRH = ln(relativeHumidity.toDouble() / 100)
        if (lnRH.isNaN() || abs(lnRH).isInfinite()) lnRH = ln(0.00001)
        val tempCalc = m * temperature / (tn + temperature)
        val top = lnRH + tempCalc
        var bottom = m - top
        if (bottom == 0.0) bottom = 0.00001
        val dewPoint = tn * top / bottom
        return dewPoint.toFloat()
    }

    /**
     * Calculates the distance of the lightning strike from the current position in meters
     * @param lightning The time the lightning was seen
     * @param thunder The time the thunder was heard
     * @return The distance to the lightning strike in meters
     */
    fun getLightningStrikeDistance(lightning: Instant, thunder: Instant): Float {
        val speedOfSound = 343f
        val duration = Duration.between(lightning, thunder)

        if (duration.isNegative || duration.isZero) {
            return 0f
        }

        val seconds = duration.toMillis() / 1000f
        return speedOfSound * seconds
    }

    /**
     * Determines if the lightning strike is close enough for concern
     * @param distance The distance to the lightning strike
     * @return true if the strike is dangerous, false otherwise
     */
    fun isLightningStrikeDangerous(distance: Distance): Boolean {
        // https://www.weather.gov/media/zhu/ZHU_Training_Page/lightning_stuff/lightning/lightning_facts.pdf
        return distance.meters().value <= 10000
    }

    /**
     * Calculates the ambient temperature from sequential temperature readings
     * @param temp0 the initial temperature (celsius)
     * @param temp1 the temperature occurring 1 time unit after temp0
     * @param temp2 the temperature occurring 2 time units after temp0
     * @return the ambient temperature in celsius or null if the readings weren't all increasing or decreasing
     */
    fun getAmbientTemperature(temp0: Float, temp1: Float, temp2: Float): Float? {
        val isIncreasing = temp0 < temp1 && temp1 < temp2
        val isDecreasing = temp0 > temp1 && temp1 > temp2

        if (!isIncreasing && !isDecreasing) {
            return null
        }
        return (temp0 * temp2 - temp1 * temp1) / (temp0 + temp2 - 2 * temp1)
    }

    fun getSeason(location: Coordinate, date: ZonedDateTime): Season {
        val north = location.isNorthernHemisphere
        val d = date.toLocalDate()
        return when {
            d >= LocalDate.of(d.year, 12, 1) -> if (north) Season.Winter else Season.Summer
            d >= LocalDate.of(d.year, 9, 1) -> if (north) Season.Fall else Season.Spring
            d >= LocalDate.of(d.year, 6, 1) -> if (north) Season.Summer else Season.Winter
            d >= LocalDate.of(d.year, 3, 1) -> if (north) Season.Spring else Season.Fall
            else -> if (north) Season.Winter else Season.Summer
        }
    }

    /**
     * Calculates the temperature at an elevation
     * @param temperature the temperature at the base elevation
     * @param baseElevation the elevation in which the temperature reading was taken
     * @param destElevation the elevation of the destination
     * @return the temperature at the destination
     */
    fun getTemperatureAtElevation(
        temperature: Temperature, baseElevation: Distance, destElevation: Distance
    ): Temperature {
        val celsius = temperature.celsius().value
        val baseMeters = baseElevation.meters().value
        val destMeters = destElevation.meters().value
        val temp = celsius - 0.0065f * (destMeters - baseMeters)
        return Temperature
            .celsius(temp.coerceAtLeast(Temperature.ABSOLUTE_ZERO.celsius().value))
            .convertTo(temperature.units)
    }

    /**
     * Get the likely precipitation types for the given cloud
     * @param cloud the type of cloud
     * @return the types of precipitation the cloud can produce
     */
    fun getPrecipitation(cloud: CloudGenus): List<Precipitation> {
        return cloudService.getPrecipitation(cloud)
    }

    /**
     * Get the likelihood that the cloud will precipitate
     * @param cloud the type of cloud
     * @return the chance that it will precipitate [0, 1]
     */
    fun getPrecipitationChance(cloud: CloudGenus): Float {
        return cloudService.getPrecipitationChance(cloud)
    }

    /**
     * Get the height range of the cloud layer
     * @param level the cloud layer
     * @param location the location
     * @return the height range of the cloud layer
     */
    fun getHeightRange(level: CloudLevel, location: Coordinate): Range<Distance> {
        return cloudService.getHeightRange(level, location)
    }

    /**
     * Get the cloud cover label
     * @param percent the percent cloud cover [0, 1]
     * @return the cloud cover classification
     */
    fun getCloudCover(percent: Float): CloudCover {
        return cloudService.getCloudCover(percent)
    }

    /**
     * Calculates the Koppen-Geiger climate classification
     * @param temperatures The average monthly temperatures
     * @param precipitation The average monthly precipitation
     * @return The Koppen-Geiger climate classification
     */
    fun getKoppenGeigerClimateClassification(
        temperatures: Map<Month, Temperature>,
        precipitation: Map<Month, Distance>
    ): KoppenGeigerClimateClassification {
        return KoppenGeigerClimateClassifier.classify(temperatures, precipitation)
    }


}
