package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.andromeda.core.math.*
import com.kylecorry.andromeda.core.time.plusHours
import com.kylecorry.andromeda.core.time.toUTCLocal
import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.eclipse.LunarEclipseParameters
import com.kylecorry.trailsensecore.domain.astronomy.locators.ICelestialLocator
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonPhase
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonTruePhase
import com.kylecorry.trailsensecore.domain.astronomy.units.*
import com.kylecorry.trailsensecore.domain.math.MathUtils
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.*

// Algorithms from Jean Meeus (Astronomical Algorithms 2nd Edition)
internal object Astro {

    fun getAltitude(
        locator: ICelestialLocator,
        ut: UniversalTime,
        location: Coordinate,
        withRefraction: Boolean = false
    ): Float {
        val coords = locator.getCoordinates(ut)
        val horizon = HorizonCoordinate.fromEquatorial(coords, ut, location)
        return horizon.let {
            if (withRefraction) {
                it.withRefraction()
            } else {
                it
            }
        }.altitude.toFloat()
    }

    fun getAzimuth(
        locator: ICelestialLocator,
        ut: UniversalTime,
        location: Coordinate
    ): Bearing {
        val coords = locator.getCoordinates(ut)
        val horizon = HorizonCoordinate.fromEquatorial(coords, ut, location)
        return Bearing(horizon.azimuth.toFloat())
    }

    // Move some of this to Time Utils others to Math Utils
    fun timeToAngle(hours: Number, minutes: Number, seconds: Number): Double {
        return timeToDecimal(hours, minutes, seconds) * 15
    }

    fun timeToDecimal(hours: Number, minutes: Number, seconds: Number): Double {
        return hours.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
    }

    /**
     * Converts an angle to between 0 and 360 degrees
     */
    fun reduceAngleDegrees(angle: Double): Double {
        return wrap(angle, 0.0, 360.0)
    }

    fun interpolate(
        n: Double,
        y1: Double,
        y2: Double,
        y3: Double
    ): Double {
        val a = y2 - y1
        val b = y3 - y2
        val c = b - a

        return y2 + (n / 2.0) * (a + b + n * c)
    }

    fun julianDay(date: LocalDateTime): Double {
        return date.toJulianDay()
    }

    /**
     * The time difference between TT and UT (TT - UT) in seconds
     */
    fun deltaT(year: Int): Double {
        val t = (year - 2000) / 100.0
        return MathUtils.polynomial(t, 102.0, 102.0, 25.3) + 0.37 * (year - 2100)
    }

    /**
     * Calculates the universal time
     */
    fun ut(time: ZonedDateTime): LocalDateTime {
        return time.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()
    }

    fun utToLocal(ut: LocalDateTime, zone: ZoneId): ZonedDateTime {
        return ut.atZone(ZoneId.of("UTC")).withZoneSameInstant(zone)
    }


    fun cube(a: Double): Double {
        return a * a * a
    }

    fun square(a: Double): Double {
        return a * a
    }


    fun ut0hOnDate(date: ZonedDateTime): LocalDateTime {
        val localDate = date.toLocalDate()

        for (i in -1..1) {
            val ut0h = ut(date.plusDays(i.toLong())).toLocalDate().atStartOfDay()
            val local0h = utToLocal(ut0h, date.zone)
            if (localDate == local0h.toLocalDate()) {
                return ut0h
            }
        }

        return ut(date).toLocalDate().atStartOfDay()
    }


}