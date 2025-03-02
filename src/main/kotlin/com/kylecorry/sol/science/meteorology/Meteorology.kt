package com.kylecorry.sol.science.meteorology

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.meteorology.clouds.CloudCover
import com.kylecorry.sol.science.meteorology.clouds.CloudGenus
import com.kylecorry.sol.science.meteorology.clouds.CloudLevel
import com.kylecorry.sol.science.meteorology.clouds.CloudService
import com.kylecorry.sol.science.meteorology.forecast.ForecastSource
import com.kylecorry.sol.science.meteorology.forecast.SolForecaster
import com.kylecorry.sol.science.meteorology.forecast.ZambrettiForecaster
import com.kylecorry.sol.science.meteorology.observation.WeatherObservation
import com.kylecorry.sol.science.shared.Season
import com.kylecorry.sol.units.*
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

object Meteorology : IWeatherService {

    private val cloudService = CloudService()

    override fun getSeaLevelPressure(
        pressure: Pressure, altitude: Quantity<Distance>, temperature: Temperature?
    ): Pressure {
        val hpa = pressure.hpa().pressure
        val meters = altitude.meters().amount
        val celsius = temperature?.celsius()?.temperature
        val adjustedPressure = if (celsius != null) {
            hpa * (1 - ((0.0065f * meters) / (celsius + 0.0065f * meters + 273.15f))).pow(
                -5.257f
            )
        } else {
            hpa * (1 - meters / 44330.0).pow(-5.255).toFloat()
        }

        return Pressure(adjustedPressure, PressureUnits.Hpa).convertTo(pressure.units)
    }

    override fun isHighPressure(pressure: Pressure): Boolean {
        return pressure.hpa().pressure >= 1022.689
    }

    override fun isLowPressure(pressure: Pressure): Boolean {
        return pressure.hpa().pressure <= 1009.144
    }

    override fun getTendency(
        last: Pressure, current: Pressure, duration: Duration, changeThreshold: Float
    ): PressureTendency {
        val diff = current.hpa().pressure - last.hpa().pressure
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

    override fun forecast(
        tendency: PressureTendency, stormThreshold: Float?
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

    override fun forecast(
        pressures: List<Reading<Pressure>>,
        clouds: List<Reading<CloudGenus?>>,
        dailyTemperatureRange: Range<Temperature>?,
        pressureChangeThreshold: Float,
        pressureStormChangeThreshold: Float,
        time: Instant,
        location: Coordinate,
        source: ForecastSource
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

    override fun forecast(
        observations: List<WeatherObservation<*>>,
        dailyTemperatureRange: Range<Temperature>?,
        pressureChangeThreshold: Float,
        pressureStormChangeThreshold: Float,
        time: Instant,
        location: Coordinate,
        source: ForecastSource
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

    override fun getHeatIndex(temperature: Float, relativeHumidity: Float): Float {
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
            c1 + c2 * temperature + c3 * relativeHumidity + c4 * temperature * relativeHumidity + c5 * temperature * temperature + c6 * relativeHumidity * relativeHumidity + c7 * temperature * temperature * relativeHumidity + c8 * temperature * relativeHumidity * relativeHumidity + c9 * temperature * temperature * relativeHumidity * relativeHumidity

        return hi.toFloat()
    }

    override fun getHeatAlert(heatIndex: Float): HeatAlert {
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

    override fun getDewPoint(temperature: Float, relativeHumidity: Float): Float {
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

    override fun getLightningStrikeDistance(lightning: Instant, thunder: Instant): Float {
        val speedOfSound = 343f
        val duration = Duration.between(lightning, thunder)

        if (duration.isNegative || duration.isZero) {
            return 0f
        }

        val seconds = duration.toMillis() / 1000f
        return speedOfSound * seconds
    }

    override fun isLightningStrikeDangerous(distance: Quantity<Distance>): Boolean {
        // https://www.weather.gov/media/zhu/ZHU_Training_Page/lightning_stuff/lightning/lightning_facts.pdf
        return distance.meters().amount <= 10000
    }

    override fun getAmbientTemperature(temp0: Float, temp1: Float, temp2: Float): Float? {
        if (!((temp0 < temp1 && temp1 < temp2) || (temp0 > temp1 && temp1 > temp2))) {
            return null
        }
        return (temp0 * temp2 - temp1 * temp1) / (temp0 + temp2 - 2 * temp1)
    }

    override fun getSeason(location: Coordinate, date: ZonedDateTime): Season {
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

    override fun getTemperatureAtElevation(
        temperature: Temperature, baseElevation: Quantity<Distance>, destElevation: Quantity<Distance>
    ): Temperature {
        val celsius = temperature.celsius().temperature
        val baseMeters = baseElevation.meters().amount
        val destMeters = destElevation.meters().amount
        val temp = celsius - 0.0065f * (destMeters - baseMeters)
        return Temperature(temp, TemperatureUnits.C).convertTo(temperature.units)
    }

    override fun getPrecipitation(cloud: CloudGenus): List<Precipitation> {
        return cloudService.getPrecipitation(cloud)
    }

    override fun getPrecipitationChance(cloud: CloudGenus): Float {
        return cloudService.getPrecipitationChance(cloud)
    }

    override fun getHeightRange(level: CloudLevel, location: Coordinate): Range<Quantity<Distance>> {
        return cloudService.getHeightRange(level, location)
    }

    override fun getCloudCover(percent: Float): CloudCover {
        return cloudService.getCloudCover(percent)
    }
}