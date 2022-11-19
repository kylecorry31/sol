package com.kylecorry.sol.science.meteorology

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.analysis.Trigonometry
import com.kylecorry.sol.science.geology.CoordinateBounds
import com.kylecorry.sol.science.geology.Geology
import com.kylecorry.sol.science.geology.WorldLandMap
import com.kylecorry.sol.units.*
import com.kylecorry.sol.science.shared.Season
import com.kylecorry.sol.science.meteorology.clouds.*
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.ln
import kotlin.math.pow

object Meteorology : IWeatherService {

    private val cloudService = CloudService()

    override fun getSeaLevelPressure(
        pressure: Pressure, altitude: Distance, temperature: Temperature?
    ): Pressure {
        val hpa = pressure.hpa().pressure
        val meters = altitude.meters().distance
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
        pressureChangeThreshold: Float,
        pressureStormChangeThreshold: Float,
        time: Instant
    ): List<WeatherForecast> {
        return WeatherForecastService.forecast(
            pressures, clouds, time, pressureChangeThreshold, pressureStormChangeThreshold
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

    override fun isLightningStrikeDangerous(distance: Distance): Boolean {
        // https://www.weather.gov/media/zhu/ZHU_Training_Page/lightning_stuff/lightning/lightning_facts.pdf
        return distance.meters().distance <= 10000
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
        temperature: Temperature, baseElevation: Distance, destElevation: Distance
    ): Temperature {
        val celsius = temperature.celsius().temperature
        val baseMeters = baseElevation.meters().distance
        val destMeters = destElevation.meters().distance
        val temp = celsius - 0.0065f * (destMeters - baseMeters)
        return Temperature(temp, TemperatureUnits.C).convertTo(temperature.units)
    }

    override fun getAverageAnnualTemperature(
        location: Coordinate, elevation: Distance
    ): Temperature {
        // http://www-das.uwyo.edu/~geerts/cwx/notes/chap16/geo_clim.html
        // Temperatures taken at 1000 hPa ~ 100m above sea level
        val latitude = location.latitude
        val temperature = Temperature.celsius(
            when {
                latitude > 16 -> 27 - 0.86 * (latitude - 16)
                latitude < -20 -> 27 - 0.63 * (latitude.absoluteValue - 20)
                else -> 27.0
            }.toFloat()
        )

        return getTemperatureAtElevation(temperature, Distance.meters(100f), elevation)
    }

    override fun getAverageAnnualTemperatureRange(
        location: Coordinate,
        elevation: Distance,
        factorInOceanWind: Boolean
    ): Range<Temperature> {
        // http://www-das.uwyo.edu/~geerts/cwx/notes/chap16/geo_clim.html
        val annual = getAverageAnnualTemperature(location, elevation).convertTo(TemperatureUnits.C)
        val distanceDownwindOfOcean =
            if (factorInOceanWind) getDistanceDownwindOfOcean(location) else null
        val range = if (distanceDownwindOfOcean != null) {
            val kilometers =
                distanceDownwindOfOcean.convertTo(DistanceUnits.Kilometers).distance.coerceAtLeast(
                    1f
                )
            (0.13 * location.latitude * kilometers.pow(0.2f)).toFloat()
        } else {
            (0.4 * location.latitude).toFloat()
        }.absoluteValue
        val min = annual.copy(temperature = annual.temperature - range / 2f)
        val max = annual.copy(temperature = annual.temperature + range / 2f)
        return Range(min, max)
    }

    override fun getAverageTemperature(
        location: Coordinate,
        elevation: Distance,
        date: LocalDate,
        factorInOceanWind: Boolean
    ): Temperature {
        val range = getAverageAnnualTemperatureRange(location, elevation, factorInOceanWind)
        val january =
            if (location.isNorthernHemisphere) Vector2(0f, range.start.temperature) else Vector2(
                0f,
                range.end.temperature
            )
        val july =
            if (location.isNorthernHemisphere) Vector2(0.5f, range.end.temperature) else Vector2(
                0.5f,
                range.start.temperature
            )
        val wave = Trigonometry.connect(january, july, 1f)
        val percent = date.dayOfYear / 365f
        return Temperature.celsius(wave.calculate(percent))
    }

    private fun getDistanceDownwindOfOcean(location: Coordinate): Distance? {
        var bounds: CoordinateBounds? = null
        var polygon: Array<DoubleArray>? = null

        val pairs = listOf(
            WorldLandMap.PapuaNewGuinea to WorldLandMap.Bounds.PapuaNewGuinea,
            WorldLandMap.Indonesia to WorldLandMap.Bounds.Indonesia,
            WorldLandMap.Australia to WorldLandMap.Bounds.Australia,
            WorldLandMap.Madagascar to WorldLandMap.Bounds.Madagascar,
            WorldLandMap.Greenland to WorldLandMap.Bounds.Greenland,
            WorldLandMap.UnitedKingdom to WorldLandMap.Bounds.UnitedKingdom,
            WorldLandMap.EurasiaAfrica to WorldLandMap.Bounds.EurasiaAfrica,
            WorldLandMap.Americas to WorldLandMap.Bounds.Americas,
        )

        for (pair in pairs) {
            if (pair.second.contains(location)) {
                bounds = pair.second
                polygon = pair.first
                break
            }
        }


        if (bounds == null || polygon == null) {
            return null
        }

        val halfBounds = if (location.isNorthernHemisphere) {
            val eastern = CoordinateBounds.from(listOf(location, bounds.center)).west
            CoordinateBounds(bounds.north, eastern, bounds.south, bounds.west)
        } else {
            val western = CoordinateBounds.from(listOf(location, bounds.center)).east
            CoordinateBounds(bounds.north, bounds.east, bounds.south, western)
        }

        val nearest = mutableListOf<Coordinate>()
        for (i in 1 until polygon.size) {
            val start = Coordinate(polygon[i - 1][1], polygon[i - 1][0])
            val end = Coordinate(polygon[i][1], polygon[i][0])
            val intersection = Geology.getNearestPoint(location, start, end)
            val latDiff = abs(intersection.latitude - location.latitude)
            if (halfBounds.contains(intersection) && latDiff < 10f) {
                nearest.add(Geology.getNearestPoint(location, start, end))
            }
        }

        if (nearest.isEmpty()) {
            return null
        }

        val closest = nearest.minBy { it.distanceTo(location) }
        return Distance.meters(closest.distanceTo(location))
    }

    override fun getPrecipitation(cloud: CloudGenus): List<Precipitation> {
        return cloudService.getPrecipitation(cloud)
    }

    override fun getPrecipitationChance(cloud: CloudGenus): Float {
        return cloudService.getPrecipitationChance(cloud)
    }

    override fun getHeightRange(level: CloudLevel, location: Coordinate): Range<Distance> {
        return cloudService.getHeightRange(level, location)
    }

    override fun getCloudCover(percent: Float): CloudCover {
        return cloudService.getCloudCover(percent)
    }
}