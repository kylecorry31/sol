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

    fun power(x: Double, power: Int): Double {
        var total = 1.0
        for (i in 0 until abs(power)) {
            total *= x
        }

        if (power < 0) {
            return 1 / total
        }

        return total
    }

    /**
     * Computes a polynomial
     * Ex. 1 + 2x + 5x^2 + x^4
     * polynomial(x, 1, 2, 5, 0, 1)
     */
    fun polynomial(x: Double, vararg coefs: Double): Double {
        var runningTotal = 0.0
        for (i in coefs.indices) {
            runningTotal += power(x, i) * coefs[i]
        }

        return runningTotal
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

    fun utFromJulianDay(jd: Double): LocalDateTime {
        return fromJulianDay(jd)
    }

    fun julianCenturies(julianDay: Double): Double {
        return (julianDay - 2451545.0) / 36525.0
    }

    /**
     * The time difference between TT and UT (TT - UT) in seconds
     */
    fun deltaT(year: Int): Double {
        val t = (year - 2000) / 100.0
        return polynomial(t, 102.0, 102.0, 25.3) + 0.37 * (year - 2100)
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

    fun meanSiderealTime(julianDay: Double): Double {
        val T = (julianDay - 2451545.0) / 36525.0
        val theta0 =
            280.46061837 + 360.98564736629 * (julianDay - 2451545.0) + 0.000387933 * square(T) - cube(
                T
            ) / 38710000.0
        return wrap(theta0, 0.0, 360.0)
    }

    fun apparentSiderealTime(
        julianDay: Double,
        longitudeNutation: Double,
        eclipticObliquity: Double
    ): Double {
        val meanSidereal = meanSiderealTime(julianDay)
        return meanSidereal + (longitudeNutation * cosDegrees(eclipticObliquity)) / 15.0
    }

    fun hourAngle(sidereal: Double, longitude: Double, rightAscension: Double): Double {
        return sidereal + longitude - rightAscension
    }

    fun hourAngle(localSidereal: Double, rightAscension: Double): Double {
        return localSidereal - rightAscension
    }

    fun azimuth(hourAngle: Double, latitude: Double, declination: Double): Double {
        return reduceAngleDegrees(
            Math.toDegrees(
                atan2(
                    sinDegrees(hourAngle),
                    cosDegrees(hourAngle) * sinDegrees(latitude) - tanDegrees(declination) * cosDegrees(
                        latitude
                    )
                )
            ) - 180
        )
    }

    fun altitude(
        hourAngle: Double,
        latitude: Double,
        declination: Double,
        withRefraction: Boolean = false
    ): Double {
        val altitude = wrap(
            Math.toDegrees(
                asin(
                    sinDegrees(latitude) * sinDegrees(declination) + cosDegrees(
                        latitude
                    ) * cosDegrees(declination) * cosDegrees(hourAngle)
                )
            ), -90.0, 90.0
        )

        return if (withRefraction) {
            val refract = wrap(refraction(altitude), -90.0, 90.0)
            wrap(altitude + refract, -90.0, 90.0)
        } else {
            altitude
        }
    }

    fun refraction(elevation: Double): Double {
        if (elevation > 85.0) {
            return 0.0
        }

        val tanElev = tanDegrees(elevation)

        if (elevation > 5.0) {
            return (58.1 / tanElev - 0.07 / cube(tanElev) + 0.000086 / power(tanElev, 5)) / 3600.0
        }

        if (elevation > -0.575) {
            return polynomial(elevation, 1735.0, -518.2, 103.4, -12.79, 0.711) / 3600.0
        }

        return -20.774 / tanElev / 3600.0
    }

    fun riseSetTransitTimes(
        latitude: Double,
        longitude: Double,
        apparentSidereal: Double,
        standardAltitude: Double,
        withRefraction: Boolean,
        deltaT: Double,
        declinations: Triple<Double, Double, Double>,
        rightAscensions: Triple<Double, Double, Double>
    ): Triple<Double, Double, Double>? {
        val cosH =
            (sinDegrees(standardAltitude) - sinDegrees(latitude) * sinDegrees(declinations.second)) / (cosDegrees(
                latitude
            ) * cosDegrees(declinations.second))

        if (cosH >= 1) {
            // Always down
            return null
        } else if (cosH <= -1) {
            // Always up
            return null
        }

        val H = wrap(Math.toDegrees(acos(cosH)), 0.0, 180.0)

        val iterations = 20
        val doneThresh = 0.0001

        var m0 = wrap((rightAscensions.second - longitude - apparentSidereal) / 360.0, 0.0, 1.0)
        var m1 = wrap(m0 - H / 360, 0.0, 1.0)
        var m2 = wrap(m0 + H / 360, 0.0, 1.0)

        for (i in 0 until iterations) {
            val sidereal0 = reduceAngleDegrees(apparentSidereal + 360.985647 * m0)
            val sidereal1 = reduceAngleDegrees(apparentSidereal + 360.985647 * m1)
            val sidereal2 = reduceAngleDegrees(apparentSidereal + 360.985647 * m2)

            val n0 = m0 + deltaT / 86400
            val n1 = m1 + deltaT / 86400
            val n2 = m2 + deltaT / 86400

            val normalizedRas = normalizeRightAscensions(rightAscensions)

            val ra0 =
                reduceAngleDegrees(
                    interpolate(
                        n0,
                        normalizedRas.first,
                        normalizedRas.second,
                        normalizedRas.third
                    )
                )
            val ra1 =
                reduceAngleDegrees(
                    interpolate(
                        n1,
                        normalizedRas.first,
                        normalizedRas.second,
                        normalizedRas.third
                    )
                )
            val ra2 = reduceAngleDegrees(
                interpolate(
                    n2,
                    normalizedRas.first,
                    normalizedRas.second,
                    normalizedRas.third
                )
            )
            val declination1 =
                interpolate(n1, declinations.first, declinations.second, declinations.third)
            val declination2 =
                interpolate(n2, declinations.first, declinations.second, declinations.third)

            val hourAngle0 = reduceAngleDegrees(hourAngle(sidereal0, longitude, ra0))
            val hourAngle1 = reduceAngleDegrees(hourAngle(sidereal1, longitude, ra1))
            val hourAngle2 = reduceAngleDegrees(hourAngle(sidereal2, longitude, ra2))

            val altitude1 = altitude(hourAngle1, latitude, declination1, withRefraction)
            val altitude2 = altitude(hourAngle2, latitude, declination2, withRefraction)

            val dm0 = -hourAngle0 / 360
            val dm1 = (altitude1 - standardAltitude) / (360 * cosDegrees(declination1) * cosDegrees(
                latitude
            ) * sinDegrees(hourAngle1))
            val dm2 = (altitude2 - standardAltitude) / (360 * cosDegrees(declination2) * cosDegrees(
                latitude
            ) * sinDegrees(hourAngle2))

            m0 = wrap(m0 + dm0, 0.0, 1.0)
            m1 = wrap(m1 + dm1, 0.0, 1.0)
            m2 = wrap(m2 + dm2, 0.0, 1.0)

            if (abs(dm0) < doneThresh && abs(dm1) < doneThresh && abs(dm2) < doneThresh) {
                break
            }
        }

        val riseHour = m1 * 24
        val transitHour = m0 * 24
        val setHour = m2 * 24

        return Triple(riseHour, transitHour, setHour)
    }

    private fun getTransitTimesHelper(
        date: ZonedDateTime,
        coordinate: Coordinate,
        standardAltitude: Double,
        withRefraction: Boolean,
        coordinateFn: (julianDate: Double) -> AstroCoordinates
    ): RiseSetTransitTimes {
        val ut = ut0hOnDate(date)
        val uty = ut0hOnDate(date.minusDays(1))
        val utt = ut0hOnDate(date.plusDays(1))
        val jd = julianDay(ut)
        val longitudeNutation = nutationInLongitude(jd)
        val eclipticObliquity = trueObliquityOfEcliptic(jd)
        val sr = apparentSiderealTime(jd, longitudeNutation, eclipticObliquity)
        val astroCoords = coordinateFn.invoke(jd)
        val astroCoordsy = coordinateFn.invoke(julianDay(uty))
        val astroCoordst = coordinateFn.invoke(julianDay(utt))
        val times = riseSetTransitTimes(
            coordinate.latitude,
            coordinate.longitude,
            sr,
            standardAltitude,
            withRefraction,
            deltaT(date.year),
            Triple(astroCoordsy.declination, astroCoords.declination, astroCoordst.declination),
            Triple(
                astroCoordsy.rightAscension,
                astroCoords.rightAscension,
                astroCoordst.rightAscension
            )
        )
            ?: return RiseSetTransitTimes(null, null, null)

        val rise = utToLocal(ut.plusHours(times.first), date.zone)
        val transit = utToLocal(ut.plusHours(times.second), date.zone)
        val set = utToLocal(ut.plusHours(times.third), date.zone)



        return RiseSetTransitTimes(rise, transit, set)
    }

    fun getTransitEvents(
        date: ZonedDateTime,
        coordinate: Coordinate,
        standardAltitude: Double,
        withRefraction: Boolean,
        coordinateFn: (julianDate: Double) -> AstroCoordinates
    ): RiseSetTransitTimes {

        val ld = date.toLocalDate()

        // Get today's times
        val today =
            getTransitTimesHelper(date, coordinate, standardAltitude, withRefraction, coordinateFn)
        if (today.rise?.toLocalDate() == ld && today.transit?.toLocalDate() == ld && today.set?.toLocalDate() == ld) {
            return today
        }

        // Today's times didn't contain all the events / were on the wrong day, check the surrounding days
        val yesterday =
            getTransitTimesHelper(
                date.minusDays(1),
                coordinate,
                standardAltitude,
                withRefraction,
                coordinateFn
            )
        val tomorrow =
            getTransitTimesHelper(
                date.plusDays(1),
                coordinate,
                standardAltitude,
                withRefraction,
                coordinateFn
            )

        val rise = listOfNotNull(
            today.rise,
            yesterday.rise,
            tomorrow.rise
        ).firstOrNull { it.toLocalDate() == date.toLocalDate() }
        val transit = listOfNotNull(
            today.transit,
            yesterday.transit,
            tomorrow.transit
        ).firstOrNull { it.toLocalDate() == date.toLocalDate() }
        val set = listOfNotNull(
            today.set,
            yesterday.set,
            tomorrow.set
        ).firstOrNull { it.toLocalDate() == date.toLocalDate() }

        return RiseSetTransitTimes(rise, transit, set)
    }

    fun moonMeanAnomaly(julianDay: Double): Double {
        val T = julianCenturies(julianDay)
        return reduceAngleDegrees(
            polynomial(
                T,
                134.9633964,
                477198.8675055,
                0.0087414,
                1 / 69699.0,
                -1 / 14712000.0
            )
        )
    }

//    fun sunToMoonMeanElongation(julianDay: Double): Double {
//        val T = (julianDay - 2451545.0) / 36525
//        return polynomial(T, 297.85036, 445267.111480, -0.0019142, 1 / 189474.0)
//    }

    fun moonArgumentOfLatitude(julianDay: Double): Double {
        val T = (julianDay - 2451545.0) / 36525
        return reduceAngleDegrees(
            polynomial(
                T,
                93.2720950,
                483202.0175233,
                -0.0036539,
                -1 / 3526000.0,
                1 / 863310000.0
            )
        )
    }

    fun moonAscendingNodeLongitude(julianDay: Double): Double {
        val T = (julianDay - 2451545.0) / 36525
        return polynomial(T, 125.04452, -1934.136261, 0.0020708, 1 / 450000.0)
    }

    fun nutationInLongitude(julianDay: Double): Double {
        val T = (julianDay - 2451545.0) / 36525
        val L = 280.4665 + 36000.7698 * T
        val LPrime = 218.3165 + 481267.8813 * T
        val omega = moonAscendingNodeLongitude(julianDay)
        return -0.004777778 * sinDegrees(omega) + 0.0003666667 * sinDegrees(2 * L) -
                0.00006388889 * sinDegrees(2 * LPrime) + 0.00005833333 * sinDegrees(2 * omega)
    }

    fun nutationInObliquity(julianDay: Double): Double {
        val T = (julianDay - 2451545.0) / 36525
        val L = 280.4665 + 36000.7698 * T
        val LPrime = 218.3165 + 481267.8813 * T
        val omega = moonAscendingNodeLongitude(julianDay)
        return 0.002555556 * cosDegrees(omega) + 0.0001583333 * cosDegrees(2 * L) +
                0.00002777778 * cosDegrees(2 * LPrime) - 0.000025 * cosDegrees(2 * omega)
    }

    fun meanObliquityOfEcliptic(julianDay: Double): Double {
        val T = julianCenturies(julianDay)
        val seconds = polynomial(T, 21.448, -46.815, -0.00059, 0.001813)
        return 23.0 + (26.0 + seconds / 60.0) / 60.0
//        return polynomial(T, 23.43929, -0.01300417, -1.638889e-7, 5.036111e-7)
    }

    fun trueObliquityOfEcliptic(julianDay: Double): Double {
        return meanObliquityOfEcliptic(julianDay) + nutationInObliquity(julianDay)
    }

    fun declination(
        apparentLongitude: Double,
        eclipticObliquity: Double,
        eclipticLatitude: Double
    ): Double {
        return asin(
            sinDegrees(eclipticLatitude) * cosDegrees(eclipticObliquity) + cosDegrees(
                eclipticLatitude
            ) * sinDegrees(eclipticObliquity) * sinDegrees(apparentLongitude)
        ).toDegrees()
    }

    /**
     * Get the current phase of the moon
     * @return The moon phase
     */
    fun getMoonPhase(time: ZonedDateTime = ZonedDateTime.now()): MoonPhase {
        val phaseAngle = getMoonPhaseAngle(time)
        val illumination = getMoonIllumination(phaseAngle).toFloat()

        for (phase in MoonTruePhase.values()) {
            if (phase.startAngle <= phaseAngle && phase.endAngle >= phaseAngle) {
                return MoonPhase(phase, illumination)
            }

            // Handle new moon
            if (phase.startAngle >= phase.endAngle) {
                if (phase.startAngle <= phaseAngle || phase.endAngle >= phaseAngle) {
                    return MoonPhase(phase, illumination)
                }
            }
        }

        return MoonPhase(MoonTruePhase.New, illumination)
    }

    fun getMoonPhaseAngle(time: ZonedDateTime): Double {
        val JDE = julianDay(time.toUTCLocal())

        val T = (JDE - 2451545) / 36525.0

        val D =
            normalizeAngle(
                297.8501921 + 445267.1114034 * T - 0.0018819 * T.pow(
                    2
                ) + T.pow(3) / 545868 - T.pow(4) / 113065000
            )

        val M = normalizeAngle(
            357.5291092 + 35999.0502909 * T - 0.0001536 * T.pow(2) + T.pow(3) / 24490000
        )

        val Mp =
            normalizeAngle(
                134.9633964 + 477198.8675055 * T - 0.0087414 * T.pow(
                    2
                ) + T.pow(3) / 69699 - T.pow(4) / 14712000
            )

        val i =
            180 - D - 6.289 * sinDegrees(Mp) + 2.100 * sinDegrees(
                M
            ) - 1.274 * sinDegrees(2 * D - Mp) - 0.658 * sinDegrees(
                2 * D
            ) - 0.214 * sinDegrees(
                2 * Mp
            ) - 0.110 * sinDegrees(D)

        return (i + 180) % 360.0
    }

    fun getMoonIllumination(phaseAngle: Double): Double {
        return ((1 + cosDegrees(phaseAngle - 180)) / 2) * 100
    }

    fun getJDEOfMeanMoonPhase(k: Double): Double {
        val T = k / 1236.85
        return 2451550.09766 + 29.530588861 * k + 0.00015437 * power(T, 2) - 0.00000015 * power(
            T,
            3
        ) + 0.00000000074 * power(T, 4)
    }

    fun getNextLunarEclipse(ut: LocalDateTime): LunarEclipseParameters {
        var k = getNextMoonPhaseK(ut, MoonTruePhase.Full)
        var T: Double
        var F: Double
        do {
            T = k / 1236.85
            F = reduceAngleDegrees(
                160.7108 + 390.67050284 * k - 0.0016118 * power(T, 2) - 0.00000227 * power(
                    T,
                    3
                ) + 0.000000011 * power(T, 4)
            )
            if (sinDegrees(F).absoluteValue > 0.36) {
                k += 1
            }
        } while (sinDegrees(F).absoluteValue > 0.36)

        val mean = getJDEOfMeanMoonPhase(k)
        val M = reduceAngleDegrees(
            2.5534 + 29.1053567 * k - 0.0000014 * power(
                T,
                2
            ) - 0.00000011 * power(T, 3)
        )
        val MPrime = reduceAngleDegrees(
            201.5643 + 385.81693528 * k + 0.0107582 * power(T, 2) + 0.00001238 * power(
                T,
                3
            ) - 0.000000058 * power(T, 4)
        )
        val omega = reduceAngleDegrees(
            124.7746 - 1.56375588 * k + 0.0020672 * power(
                T,
                2
            ) + 0.00000215 * power(T, 3)
        )
        val E = polynomial(T, 1.0, -0.002516, -0.0000074)

        val F1 = F - 0.02665 * sinDegrees(omega)
        val A1 = 299.77 + 0.107408 * k - 0.009173 * power(T, 2)

        var correction = 0.0
        val table = table54_1()
        for (row in table) {
            correction += (row[0] / 10000.0) * (if (row[1] == 0) 1.0 else E) * sinDegrees(
                row[2] * M + row[3] * MPrime + row[4] * F1 + row[5] * A1 + row[6] * omega
            )
        }

        val correctedJD = mean + correction

        var p = 0.0
        val tableP = table54_P()
        for (row in tableP) {
            p += (row[0] / 10000.0) * (if (row[1] == 0) 1.0 else E) * sinDegrees(
                row[2] * M + row[3] * MPrime + row[4] * F1
            )
        }

        var q = 0.0
        val tableQ = table54_Q()
        for (row in tableQ) {
            q += (row[0] / 10000.0) * (if (row[1] == 0) 1.0 else E) * cosDegrees(
                row[2] * M + row[3] * MPrime
            )
        }

        val W = cosDegrees(F1).absoluteValue

        val gamma = (p * cosDegrees(F1) + q * sinDegrees(F1)) * (1 - 0.0048 * W)
        val u =
            0.0059 + 0.0046 * E * cosDegrees(M) - 0.0182 * cosDegrees(MPrime) + 0.0004 * cosDegrees(
                2 * MPrime
            ) - 0.0005 * cosDegrees(M + MPrime)

        val n = 0.5458 + 0.04 * cosDegrees(MPrime)

        val datetime = ZonedDateTime.of(utFromJulianDay(correctedJD), ZoneId.of("UTC"))

        return LunarEclipseParameters(
            datetime.toInstant().minusSeconds(deltaT(datetime.year).toLong()),
            gamma,
            u,
            n
        )

    }

    fun getNextMoonPhaseK(date: LocalDateTime, moonTruePhase: MoonTruePhase): Double {
        val year = date.year
        val day = date.dayOfYear / 365.25
        val hour = (date.hour / 24.0) / 365.25
        val minute = (((date.minute / 60.0) / 24.0) / 365.25)
        val y = year + day + hour + minute
        val k = (y - 2000) * 12.3685

        val ending = when (moonTruePhase) {
            MoonTruePhase.New -> 0.0
            MoonTruePhase.WaningCrescent -> 0.125
            MoonTruePhase.ThirdQuarter -> 0.25
            MoonTruePhase.WaningGibbous -> 0.375
            MoonTruePhase.Full -> 0.5
            MoonTruePhase.WaxingGibbous -> 0.625
            MoonTruePhase.FirstQuarter -> 0.75
            MoonTruePhase.WaxingCrescent -> 0.875
        }

        val intK = floor(k)
        val remainder = k % 1

        return if (remainder > ending) {
            intK + 1.0 + ending
        } else {
            intK + ending
        }
    }

    private fun normalizeRightAscensions(rightAscensions: Triple<Double, Double, Double>): Triple<Double, Double, Double> {
        val ra1 = rightAscensions.first
        val ra2 = if (rightAscensions.second < ra1) {
            rightAscensions.second + 360
        } else {
            rightAscensions.second
        }
        val ra3 = if (rightAscensions.third < ra2) {
            rightAscensions.third + 360
        } else {
            rightAscensions.third
        }
        return Triple(ra1, ra2, ra3)
    }

    fun cube(a: Double): Double {
        return a * a * a
    }

    fun square(a: Double): Double {
        return a * a
    }


    private fun ut0hOnDate(date: ZonedDateTime): LocalDateTime {
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

    private fun table54_1(): Array<Array<Int>> {
        return arrayOf(
            // [term, E bool, M, Mprime, F prime, A prime, omega]
            arrayOf(-4065, 0, 0, 1, 0, 0, 0),
            arrayOf(1727, 1, 1, 0, 0, 0, 0),
            arrayOf(161, 0, 0, 2, 0, 0, 0),
            arrayOf(-97, 0, 0, 0, 2, 0, 0),
            arrayOf(73, 1, -1, 1, 0, 0, 0),
            arrayOf(-50, 1, 1, 1, 0, 0, 0),
            arrayOf(-23, 0, 0, 1, -2, 0, 0),
            arrayOf(21, 1, 2, 0, 0, 0, 0),
            arrayOf(12, 0, 0, 1, 2, 0, 0),
            arrayOf(6, 1, 1, 2, 0, 0, 0),
            arrayOf(-4, 0, 0, 3, 0, 0, 0),
            arrayOf(-3, 1, 1, 0, 2, 0, 0),
            arrayOf(3, 0, 0, 0, 0, 1, 0),
            arrayOf(-2, 1, 1, 0, -2, 0, 0),
            arrayOf(-2, 1, -1, 2, 0, 0, 0),
            arrayOf(-2, 0, 0, 0, 0, 0, 1),
        )
    }

    private fun table54_P(): Array<Array<Int>> {
        return arrayOf(
            // [term, E bool, M, Mprime, F prime]
            arrayOf(2070, 1, 1, 0, 0),
            arrayOf(24, 1, 2, 0, 0),
            arrayOf(-392, 0, 0, 1, 0),
            arrayOf(116, 0, 0, 2, 0),
            arrayOf(-73, 1, 1, 1, 0),
            arrayOf(67, 1, -1, 1, 0),
            arrayOf(118, 0, 0, 0, 2),
        )
    }

    private fun table54_Q(): Array<Array<Int>> {
        return arrayOf(
            // [term, E bool, M, Mprime]
            arrayOf(52207, 0, 0, 0),
            arrayOf(-48, 1, 1, 0),
            arrayOf(20, 1, 2, 0),
            arrayOf(-3299, 0, 0, 1),
            arrayOf(-60, 1, 1, 1),
            arrayOf(41, 1, -1, 1),
        )
    }

    private fun table47a(): Array<Array<Int>> {
        return arrayOf(
            arrayOf(0, 0, 1, 0, 6288774, -20905355),
            arrayOf(2, 0, -1, 0, 1274027, -3699111),
            arrayOf(2, 0, 0, 0, 658314, -2955968),
            arrayOf(0, 0, 2, 0, 213618, -569925),
            arrayOf(0, 1, 0, 0, -185116, 48888),
            arrayOf(0, 0, 0, 2, -114332, -3149),
            arrayOf(2, 0, -2, 0, 58793, 246158),
            arrayOf(2, -1, -1, 0, 57066, -152138),
            arrayOf(2, 0, 1, 0, 53322, -170733),
            arrayOf(2, -1, 0, 0, 45758, -204586),
            arrayOf(0, 1, -1, 0, -40923, -129620),
            arrayOf(1, 0, 0, 0, -34720, 108743),
            arrayOf(0, 1, 1, 0, -30383, 104755),
            arrayOf(2, 0, 0, -2, 15327, 10321),
            arrayOf(0, 0, 1, 2, -12528, 0),
            arrayOf(0, 0, 1, -2, 10980, 79661),
            arrayOf(4, 0, -1, 0, 10675, -34782),
            arrayOf(0, 0, 3, 0, 10034, -23210),
            arrayOf(4, 0, -2, 0, 8548, -21636),
            arrayOf(2, 1, -1, 0, -7888, 24208),
            arrayOf(2, 1, 0, 0, -6766, 30824),
            arrayOf(1, 0, -1, 0, -5163, -8379),
            arrayOf(1, 1, 0, 0, 4987, -16675),
            arrayOf(2, -1, 1, 0, 4036, -12831),
            arrayOf(2, 0, 2, 0, 3994, -10445),
            arrayOf(4, 0, 0, 0, 3861, -11650),
            arrayOf(2, 0, -3, 0, 3665, 14403),
            arrayOf(0, 1, -2, 0, -2689, -7003),
            arrayOf(2, 0, -1, 2, -2602, 0),
            arrayOf(2, -1, -2, 0, 2390, 10056),
            arrayOf(1, 0, 1, 0, -2348, 6322),
            arrayOf(2, -2, 0, 0, 2236, -9884),
            arrayOf(0, 1, 2, 0, -2120, 5751),
            arrayOf(0, 2, 0, 0, -2069, 0),
            arrayOf(2, -2, -1, 0, 2048, -4950),
            arrayOf(2, 0, 1, -2, -1773, 4130),
            arrayOf(2, 0, 0, 2, -1595, 0),
            arrayOf(4, -1, -1, 0, 1215, -3958),
            arrayOf(0, 0, 2, 2, -1110, 0),
            arrayOf(3, 0, -1, 0, -892, 3258),
            arrayOf(2, 1, 1, 0, -810, 2616),
            arrayOf(4, -1, -2, 0, 759, -1897),
            arrayOf(0, 2, -1, 0, -713, -2117),
            arrayOf(2, 2, -1, 0, -700, 2354),
            arrayOf(2, 1, -2, 0, 691, 0),
            arrayOf(2, -1, 0, -2, 596, 0),
            arrayOf(4, 0, 1, 0, 549, -1423),
            arrayOf(0, 0, 4, 0, 537, -1117),
            arrayOf(4, -1, 0, 0, 520, -1571),
            arrayOf(1, 0, -2, 0, -487, -1739),
            arrayOf(2, 1, 0, -2, -399, 0),
            arrayOf(0, 0, 2, -2, -381, -4421),
            arrayOf(1, 1, 1, 0, 351, 0),
            arrayOf(3, 0, -2, 0, -340, 0),
            arrayOf(4, 0, -3, 0, 330, 0),
            arrayOf(2, -1, 2, 0, 327, 0),
            arrayOf(0, 2, 1, 0, -323, 1165),
            arrayOf(1, 1, -1, 0, 299, 0),
            arrayOf(2, 0, 3, 0, 294, 0),
            arrayOf(2, 0, -1, -2, 0, 8752)
        )
    }

    private fun table47b(): Array<Array<Int>> {
        return arrayOf(
            arrayOf(0, 0, 0, 1, 5128122),
            arrayOf(0, 0, 1, 1, 280602),
            arrayOf(0, 0, 1, -1, 277693),
            arrayOf(2, 0, 0, -1, 173237),
            arrayOf(2, 0, -1, 1, 55413),
            arrayOf(2, 0, -1, -1, 46271),
            arrayOf(2, 0, 0, 1, 32573),
            arrayOf(0, 0, 2, 1, 17198),
            arrayOf(2, 0, 1, -1, 9266),
            arrayOf(0, 0, 2, -1, 8822),
            arrayOf(2, -1, 0, -1, 8216),
            arrayOf(2, 0, -2, -1, 4324),
            arrayOf(2, 0, 1, 1, 4200),
            arrayOf(2, 1, 0, -1, -3359),
            arrayOf(2, -1, -1, 1, 2463),
            arrayOf(2, -1, 0, 1, 2211),
            arrayOf(2, -1, -1, -1, 2065),
            arrayOf(0, 1, -1, -1, -1870),
            arrayOf(4, 0, -1, -1, 1828),
            arrayOf(0, 1, 0, 1, -1794),
            arrayOf(0, 0, 0, 3, -1749),
            arrayOf(0, 1, -1, 1, -1565),
            arrayOf(1, 0, 0, 1, -1491),
            arrayOf(0, 1, 1, 1, -1475),
            arrayOf(0, 1, 1, -1, -1410),
            arrayOf(0, 1, 0, -1, -1344),
            arrayOf(1, 0, 0, -1, -1335),
            arrayOf(0, 0, 3, 1, 1107),
            arrayOf(4, 0, 0, -1, 1021),
            arrayOf(4, 0, -1, 1, 833),
            arrayOf(0, 0, 1, -3, 777),
            arrayOf(4, 0, -2, 1, 671),
            arrayOf(2, 0, 0, -3, 607),
            arrayOf(2, 0, 2, -1, 596),
            arrayOf(2, -1, 1, -1, 491),
            arrayOf(2, 0, -2, 1, -451),
            arrayOf(0, 0, 3, -1, 439),
            arrayOf(2, 0, 2, 1, 422),
            arrayOf(2, 0, -3, -1, 421),
            arrayOf(2, 1, -1, 1, -366),
            arrayOf(2, 1, 0, 1, -351),
            arrayOf(4, 0, 0, 1, 331),
            arrayOf(2, -1, 1, 1, 315),
            arrayOf(2, -2, 0, -1, 302),
            arrayOf(0, 0, 1, 3, -283),
            arrayOf(2, 1, 1, -1, -229),
            arrayOf(1, 1, 0, -1, 223),
            arrayOf(1, 1, 0, 1, 223),
            arrayOf(0, 1, -2, -1, -220),
            arrayOf(2, 1, -1, -1, -220),
            arrayOf(1, 0, 1, 1, -185),
            arrayOf(2, -1, -2, -1, 181),
            arrayOf(0, 1, 2, 1, -177),
            arrayOf(4, 0, -2, -1, 176),
            arrayOf(4, -1, -1, -1, 166),
            arrayOf(1, 0, 1, -1, -164),
            arrayOf(4, 0, 1, -1, 132),
            arrayOf(1, 0, -1, -1, -119),
            arrayOf(4, -1, 0, -1, 115),
            arrayOf(2, -2, 0, 1, 107)
        )
    }
}


data class AstroCoordinates(
    val declination: Double,
    val rightAscension: Double,
    val distanceKm: Double? = null
)
