package com.kylecorry.sol.science.oceanography

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.optimization.IExtremaFinder
import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.science.astronomy.moon.MoonTruePhase
import com.kylecorry.sol.science.geology.Geology
import com.kylecorry.sol.science.oceanography.waterlevel.IWaterLevelCalculator
import com.kylecorry.sol.time.Time
import com.kylecorry.sol.units.*
import java.time.Duration
import java.time.ZonedDateTime

class OceanographyService : IOceanographyService {

    override fun getTidalRange(time: ZonedDateTime): TidalRange {
        for (i in 0..3) {
            val phase = Astronomy.getMoonPhase(time.minusDays(i.toLong()))

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

    override fun getTides(
        waterLevelCalculator: IWaterLevelCalculator,
        start: ZonedDateTime,
        end: ZonedDateTime,
        extremaFinder: IExtremaFinder
    ): List<Tide> {
        val range = Duration.between(start, end).toMinutes()
        val extrema = extremaFinder.find(Range(0.0, range.toDouble())) {
            waterLevelCalculator.calculate(start.plusMinutes(it.toLong())).toDouble()
        }
        return extrema.map { Tide(start.plusMinutes(it.point.x.toLong()), it.isHigh, it.point.y) }
    }

    override fun getDepth(
        pressure: Pressure,
        seaLevelPressure: Pressure,
        isSaltWater: Boolean
    ): Distance {
        if (pressure <= seaLevelPressure) {
            return Distance.from(0f, DistanceUnits.Meters)
        }

        val waterDensity = if (isSaltWater) DENSITY_SALT_WATER else DENSITY_FRESH_WATER
        val pressureDiff =
            pressure.convertTo(PressureUnits.Hpa).value - seaLevelPressure.convertTo(
                PressureUnits.Hpa
            ).value

        return Distance.from(
            pressureDiff * 100 / (Geology.GRAVITY * waterDensity),
            DistanceUnits.Meters
        )
    }

    override fun getLunitidalInterval(highTideTime: ZonedDateTime, location: Coordinate): Duration? {
        // Step 1: Get the time of the moon transit prior to the high tide
        val over = getLastMoonTransit(location, highTideTime)
        val under = getLastMoonUnderfootTime(location, highTideTime)
        val lastTransit = Time.getClosestPastTime(highTideTime, listOf(over, under)) ?: return null

        // Step 2: Calculate the time between the moon transit and the high tide
        var duration = Duration.between(lastTransit, highTideTime)

        while (duration > Duration.ofHours(12)) {
            duration -= Duration.ofHours(12)
        }

        return duration
    }

    override fun getMeanLunitidalInterval(
        highTideTimes: List<ZonedDateTime>,
        location: Coordinate
    ): Duration? {
        // TODO: Give more weight to tides closer to full/new moon?
        val intervals = highTideTimes.mapNotNull { getLunitidalInterval(it, location) }
        if (intervals.isEmpty()) {
            return null
        }

        var averageDuration = Duration.ZERO
        val needsCorrection = intervals.any { it > Duration.ofHours(8) } && intervals.any { it < Duration.ofHours(4) }

        for (i in intervals.indices) {
            var interval = intervals[i]
            if (needsCorrection) {
                if (interval < Duration.ofHours(4)) {
                    interval += Duration.ofHours(12)
                }
            }
            averageDuration = averageDuration.plus(interval)
        }

        averageDuration = averageDuration.dividedBy(intervals.size.toLong())

        while (averageDuration > Duration.ofHours(12)) {
            averageDuration -= Duration.ofHours(12)
        }

        return averageDuration
    }

    private fun getLastMoonTransit(location: Coordinate, time: ZonedDateTime): ZonedDateTime? {
        val todayMoon = Astronomy.getMoonEvents(time, location).transit
        val yesterdayMoon = Astronomy.getMoonEvents(time.minusDays(1), location).transit
        val tomorrowMoon = Astronomy.getMoonEvents(time.plusDays(1), location).transit

        return Time.getClosestPastTime(time, listOf(yesterdayMoon, todayMoon, tomorrowMoon))
    }

    private fun getLastMoonUnderfootTime(location: Coordinate, time: ZonedDateTime): ZonedDateTime? {
        return getLastMoonTransit(Coordinate(-location.latitude, location.longitude + 180), time)
    }

    companion object {
        const val DENSITY_SALT_WATER = 1023.6f
        const val DENSITY_FRESH_WATER = 997.0474f
    }

}