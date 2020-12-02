package com.kylecorry.trailsensecore.domain.weather

import java.time.Duration
import java.time.Instant
import kotlin.math.abs
import kotlin.math.ln

class WeatherService : IWeatherService {
    override fun getTendency(
        last: PressureReading,
        current: PressureReading,
        changeThreshold: Float
    ): PressureTendency {
        val diff = current.value - last.value
        val dt = Duration.between(last.time, current.time).seconds

        if (dt == 0L) {
            return PressureTendency(PressureCharacteristic.Steady, 0f)
        }

        val changeAmt = (diff / dt) * 60 * 60 * 3

        val fastThreshold = changeThreshold + 2

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
        tendency: PressureTendency,
        currentPressure: PressureReading,
        stormThreshold: Float?
    ): Weather {
        val isStorm = tendency.amount <= (stormThreshold ?: -6f)

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

        val hi = c1 +
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

        if (duration.isNegative || duration.isZero){
            return 0f
        }

        val seconds = duration.toMillis() / 1000f
        return speedOfSound * seconds
    }
}