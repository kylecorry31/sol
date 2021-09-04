package com.kylecorry.trailsensecore.domain.oceanography

import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.andromeda.core.units.DistanceUnits
import com.kylecorry.andromeda.core.units.Pressure
import com.kylecorry.andromeda.core.units.PressureUnits
import com.kylecorry.trailsensecore.domain.astronomy.AstronomyService
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonTruePhase
import com.kylecorry.trailsensecore.domain.physics.PhysicsService
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

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

    override fun getTideType(referenceHighTide: ZonedDateTime, now: ZonedDateTime): TideType {
        val nextTide = getNextTide(referenceHighTide, now) ?: return TideType.Half
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

    override fun getNextTide(referenceHighTide: ZonedDateTime, now: ZonedDateTime): Tide? {
        val today = getTides(referenceHighTide, now.toLocalDate())
        val tomorrow = getTides(referenceHighTide, now.toLocalDate().plusDays(1))

        return (today + tomorrow).firstOrNull {
            it.time > now
        }
    }

    override fun getTides(referenceHighTide: ZonedDateTime, date: LocalDate): List<Tide> {
        val averageLunarDay = Duration.ofHours(24).plusMinutes(50).plusSeconds(30)
        val halfLunarDay = averageLunarDay.dividedBy(2)
        val quarterLunarDay = averageLunarDay.dividedBy(4)
        var highTideOnDate = referenceHighTide
        while (highTideOnDate.toLocalDate() != date) {
            highTideOnDate = if (highTideOnDate.toLocalDate() > date) {
                highTideOnDate.minus(halfLunarDay)
            } else {
                highTideOnDate.plus(halfLunarDay)
            }
        }

        val tides = listOf(
            Tide(highTideOnDate.minus(halfLunarDay), TideType.High),
            Tide(highTideOnDate, TideType.High),
            Tide(highTideOnDate.plus(halfLunarDay), TideType.High),
            Tide(highTideOnDate.minus(halfLunarDay).minus(quarterLunarDay), TideType.Low),
            Tide(highTideOnDate.minus(quarterLunarDay), TideType.Low),
            Tide(highTideOnDate.plus(quarterLunarDay), TideType.Low),
            Tide(highTideOnDate.plus(halfLunarDay).plus(quarterLunarDay), TideType.Low),
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
            pressureDiff * 100 / (PhysicsService.GRAVITY * waterDensity),
            DistanceUnits.Meters
        )
    }

    companion object {
        const val DENSITY_SALT_WATER = 1023.6f
        const val DENSITY_FRESH_WATER = 997.0474f
    }

}