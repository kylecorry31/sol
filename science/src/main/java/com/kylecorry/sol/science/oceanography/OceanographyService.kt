package com.kylecorry.sol.science.oceanography

import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.science.astronomy.AstronomyService
import com.kylecorry.sol.science.astronomy.moon.MoonTruePhase
import com.kylecorry.sol.science.geology.GeologyService
import com.kylecorry.sol.time.Time.atStartOfDay
import com.kylecorry.sol.units.*
import java.time.*

class OceanographyService : IOceanographyService {

    private val astronomyService = AstronomyService()

    override fun getTidalRange(time: ZonedDateTime): TidalRange {
        for (i in 0..3) {
            val phase = astronomyService.getMoonPhase(time.minusDays(i.toLong()))

            when (phase.phase) {
                MoonTruePhase.New, MoonTruePhase.Full -> {
                    return TidalRange.Spring
                }
                MoonTruePhase.FirstQuarter, MoonTruePhase.ThirdQuarter -> {
                    return TidalRange.Neap
                }
                else -> {
                    // Do nothing
                }
            }
        }

        return TidalRange.Normal
    }

    override fun getTides(harmonics: List<TidalHarmonic>, date: ZonedDateTime): List<Tide> {
        // Get water levels for the day
        val levels = mutableListOf<Float>()
        var time = date.atStartOfDay()
        while (time.toLocalDate() == date.toLocalDate()) {
            levels.add(getWaterLevel(harmonics, time))
            time = time.plusMinutes(1)
        }

        // Find highs / lows
        val tides = mutableListOf<Tide>()
        for (i in 1 until levels.lastIndex) {
            val isHigh = levels[i - 1] < levels[i] && levels[i + 1] < levels[i]
            val isLow = levels[i - 1] > levels[i] && levels[i + 1] > levels[i]

            if (isHigh) {
                val tideTime = date.atStartOfDay().plusMinutes(i.toLong())
                tides.add(Tide.high(tideTime))
            }

            if (isLow) {
                val tideTime = date.atStartOfDay().plusMinutes(i.toLong())
                tides.add(Tide.low(tideTime))
            }
        }

        return tides
    }

    override fun getDepth(
        pressure: Pressure,
        seaLevelPressure: Pressure,
        isSaltWater: Boolean
    ): Distance {
        if (pressure <= seaLevelPressure) {
            return Distance(0f, DistanceUnits.Meters)
        }

        val waterDensity = if (isSaltWater) DENSITY_SALT_WATER else DENSITY_FRESH_WATER
        val pressureDiff =
            pressure.convertTo(PressureUnits.Hpa).pressure - seaLevelPressure.convertTo(
                PressureUnits.Hpa
            ).pressure

        return Distance(
            pressureDiff * 100 / (GeologyService.GRAVITY * waterDensity),
            DistanceUnits.Meters
        )
    }

    override fun estimateHarmonics(
        highTide: ZonedDateTime,
        frequency: TideFrequency,
        amplitude: Float
    ): List<TidalHarmonic> {
        val start = ZonedDateTime.of(
            LocalDateTime.of(highTide.year, 1, 1, 0, 0),
            ZoneId.of("UTC")
        )
        val t = Duration.between(start, highTide).seconds / 3600f
        val year = highTide.year
        val constituent = when (frequency) {
            TideFrequency.Diurnal -> TideConstituent.K1
            TideFrequency.Semidiurnal -> TideConstituent.M2
        }
        val constituentPhase = AstronomicalArgumentCalculator.get(constituent, year)

        val phase = constituentPhase + constituent.speed * t

        return listOf(
            TidalHarmonic(constituent, amplitude, phase)
        )
    }

    override fun getWaterLevel(
        harmonics: List<TidalHarmonic>,
        time: ZonedDateTime
    ): Float {
        val start = ZonedDateTime.of(
            LocalDateTime.of(time.year, 1, 1, 0, 0),
            ZoneId.of("UTC")
        )
        val t = Duration.between(start, time).seconds / 3600f
        val year = time.year
        val heights = harmonics.map {
            val constituentPhase = AstronomicalArgumentCalculator.get(it.constituent, year)
            val nodeFactor = NodeFactorCalculator.get(
                it.constituent,
                year
            ) // TODO: Is this needed if the reference date is provided?
            nodeFactor * it.amplitude * cosDegrees(it.constituent.speed * t + constituentPhase - it.phase)
        }
        return heights.sum()
    }


    companion object {
        const val DENSITY_SALT_WATER = 1023.6f
        const val DENSITY_FRESH_WATER = 997.0474f
    }

}