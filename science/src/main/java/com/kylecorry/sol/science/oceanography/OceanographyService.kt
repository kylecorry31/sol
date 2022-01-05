package com.kylecorry.sol.science.oceanography

import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.science.astronomy.AstronomyService
import com.kylecorry.sol.science.astronomy.moon.MoonTruePhase
import com.kylecorry.sol.science.geology.GeologyService
import com.kylecorry.sol.time.Time.atStartOfDay
import com.kylecorry.sol.units.*
import java.time.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

    override fun getTideType(
        referenceHighTide: ZonedDateTime,
        frequency: TideFrequency,
        now: ZonedDateTime
    ): TideType {
        val nextTide = getNextTide(referenceHighTide, frequency, now) ?: return TideType.Half
        val timeToNextTide = Duration.between(now, nextTide.time)
        return if (nextTide.type == TideType.High && timeToNextTide < Duration.ofHours(2) || (nextTide.type == TideType.Low && timeToNextTide > Duration.ofHours(
                4
            ))
        ) {
            TideType.High
        } else if (nextTide.type == TideType.Low && timeToNextTide < Duration.ofHours(2) || (nextTide.type == TideType.High && timeToNextTide > Duration.ofHours(
                4
            ))
        ) {
            TideType.Low
        } else {
            TideType.Half
        }
    }

    override fun getNextTide(
        referenceHighTide: ZonedDateTime,
        frequency: TideFrequency,
        now: ZonedDateTime
    ): Tide? {
        val today = getTides(referenceHighTide, frequency, now.toLocalDate())
        val tomorrow = getTides(referenceHighTide, frequency, now.toLocalDate().plusDays(1))

        return (today + tomorrow).firstOrNull {
            it.time > now
        }
    }

    override fun getTides(
        referenceHighTide: ZonedDateTime,
        frequency: TideFrequency,
        date: LocalDate
    ): List<Tide> {
        val averageLunarDay = Duration.ofHours(24).plusMinutes(50).plusSeconds(30)
        val tideCycle = when (frequency) {
            TideFrequency.Diurnal -> averageLunarDay
            TideFrequency.Semidiurnal -> averageLunarDay.dividedBy(2)
        }
        val halfTideCycle = tideCycle.dividedBy(2)
        var highTideOnDate = referenceHighTide
        while (highTideOnDate.toLocalDate() != date) {
            highTideOnDate = if (highTideOnDate.toLocalDate() > date) {
                highTideOnDate.minus(tideCycle)
            } else {
                highTideOnDate.plus(tideCycle)
            }
        }

        val tides = listOf(
            Tide(highTideOnDate.minus(tideCycle), TideType.High),
            Tide(highTideOnDate, TideType.High),
            Tide(highTideOnDate.plus(tideCycle), TideType.High),
            Tide(highTideOnDate.minus(tideCycle).minus(halfTideCycle), TideType.Low),
            Tide(highTideOnDate.minus(halfTideCycle), TideType.Low),
            Tide(highTideOnDate.plus(halfTideCycle), TideType.Low),
            Tide(highTideOnDate.plus(tideCycle).plus(halfTideCycle), TideType.Low),
        )

        return tides.filter { it.time.toLocalDate() == date }.sortedBy { it.time }
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

    override fun getWaterLevel(
        time: ZonedDateTime,
        harmonics: List<TidalHarmonic>,
        isLocal: Boolean,
        referenceTime: ZonedDateTime?
    ): Float {
        val start = referenceTime ?: ZonedDateTime.of(
            LocalDateTime.of(time.year, 1, 1, 0, 0),
            ZoneId.of("UTC")
        )
        val t = Duration.between(start, time).seconds / 3600f
        val year = time.year
        val heights = harmonics.map {
            val constituentPhase =
                if (isLocal) 0f else AstronomicalArgumentCalculator.get(it.constituent, year)
            val nodeFactor = NodeFactorCalculator.get(it.constituent, year) // TODO: Is this needed if the reference date is provided?
            nodeFactor * it.amplitude * cosDegrees(it.constituent.speed * t + constituentPhase - it.phase)
        }
        return heights.sum()
    }


    companion object {
        const val DENSITY_SALT_WATER = 1023.6f
        const val DENSITY_FRESH_WATER = 997.0474f
    }

}