package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.trailsensecore.domain.Bearing
import com.kylecorry.trailsensecore.domain.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonPhase
import com.kylecorry.trailsensecore.domain.astronomy.sun.SunTimesMode
import com.kylecorry.trailsensecore.domain.time.DateUtils
import java.time.ZonedDateTime

class AstronomyService : IAstronomyService {

    private val oldService = OldAstronomyService()

    override fun getSunEvents(
        date: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode
    ): RiseSetTransitTimes {
        val times = oldService.getSunTimes(location, mode, date.toLocalDate())
        val noon = oldService.getSolarNoon(location, date.toLocalDate())

        val rise = times.up?.atZone(date.zone)
        val set = times.down?.atZone(date.zone)
        val transit = noon?.atZone(date.zone)

        return RiseSetTransitTimes(rise, transit, set)
    }

    override fun getSunAltitude(time: ZonedDateTime, location: Coordinate): Float {
        return oldService.getSunAltitude(location, time.toLocalDateTime()).altitudeDegrees
    }

    override fun getSunAzimuth(time: ZonedDateTime, location: Coordinate): Bearing {
        return oldService.getSunAzimuth(location, time.toLocalDateTime())
    }

    override fun getNextSunset(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode
    ): ZonedDateTime? {
        val today = getSunEvents(time, location, mode)
        val tomorrow = getSunEvents(time.plusDays(1), location, mode)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.set, tomorrow.set)
        )
    }

    override fun getNextSunrise(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode
    ): ZonedDateTime? {
        val today = getSunEvents(time, location, mode)
        val tomorrow = getSunEvents(time.plusDays(1), location, mode)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.rise, tomorrow.rise)
        )
    }

    override fun getMoonEvents(date: ZonedDateTime, location: Coordinate): RiseSetTransitTimes {
        val times = oldService.getMoonTimes(location, date.toLocalDate())
        val noon = oldService.getLunarNoon(location, date.toLocalDate())

        val rise = times.up?.atZone(date.zone)
        val set = times.down?.atZone(date.zone)
        val transit = noon?.atZone(date.zone)

        return RiseSetTransitTimes(rise, transit, set)
    }

    override fun getMoonAltitude(time: ZonedDateTime, location: Coordinate): Float {
        return oldService.getMoonAltitude(location, time.toLocalDateTime()).altitudeDegrees
    }

    override fun getMoonAzimuth(time: ZonedDateTime, location: Coordinate): Bearing {
        return oldService.getMoonAzimuth(location, time.toLocalDateTime())
    }

    override fun getNextMoonset(time: ZonedDateTime, location: Coordinate): ZonedDateTime? {
        val today = getMoonEvents(time, location)
        val tomorrow = getMoonEvents(time.plusDays(1), location)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.set, tomorrow.set)
        )
    }

    override fun getNextMoonrise(time: ZonedDateTime, location: Coordinate): ZonedDateTime? {
        val today = getMoonEvents(time, location)
        val tomorrow = getMoonEvents(time.plusDays(1), location)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.rise, tomorrow.rise)
        )
    }

    override fun getMoonPhase(date: ZonedDateTime): MoonPhase {
        return oldService.getMoonPhase(date.toLocalDate())
    }

}