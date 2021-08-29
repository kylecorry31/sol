package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.andromeda.core.math.*
import com.kylecorry.andromeda.core.time.plusHours
import com.kylecorry.andromeda.core.time.toUTCLocal
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.eclipse.LunarEclipseParameters
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonPhase
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonTruePhase
import java.time.*
import kotlin.collections.toDoubleArray
import kotlin.math.*

// Algorithms from Jean Meeus (Astronomical Algorithms 2nd Edition)
internal object Astro {

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

    fun canInterpolate(y1: Double, y2: Double, y3: Double, threshold: Double): Boolean {
        val a = y2 - y1
        val b = y3 - y2
        return abs(b - a) < threshold
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

    fun interpolateExtremum(y1: Double, y2: Double, y3: Double): Double {
        val a = y2 - y1
        val b = y3 - y2
        val c = b - a

        return y2 - square(a + b) / (8 * c)
    }

    fun interpolateExtremumX(y1: Double, y2: Double, y3: Double): Double {
        val a = y2 - y1
        val b = y3 - y2
        val c = b - a

        return -(a + b) / (2 * c)
    }

    fun interpolateZeroCrossing(y1: Double, y2: Double, y3: Double): Double {
        val a = y2 - y1
        val b = y3 - y2
        val c = b - a

        val negligibleThreshold = 1e-12
        val iterations = 20

        var estimate = 0.0

        for (i in 0 until iterations) {
            val lastEstimate = estimate

            val gradient =
                -(2 * y2 + estimate * (a + b + c * estimate)) / (a + b + 2 * c * estimate)
            estimate += gradient

            if (abs(estimate - lastEstimate) <= negligibleThreshold) {
                break
            }
        }

        return estimate
    }

    fun julianDay(date: LocalDateTime): Double {
        var Y = date.year.toDouble()
        var M = date.month.value.toDouble()
        val D =
            date.dayOfMonth.toDouble() + timeToAngle(date.hour, date.minute, date.second) / 360.0

        if (M <= 2) {
            Y--
            M += 12
        }

        val A = floor(Y / 100)
        val B = 2 - A + floor(A / 4)

        return floor(365.25 * (Y + 4716)) + floor(30.6001 * (M + 1)) + D + B - 1524.5
    }

    fun utFromJulianDay(jd: Double): LocalDateTime {
        val f = (jd + 0.5) % 1
        val z = (jd + 0.5) - f

        val a = if (z < 2299161) {
            z
        } else {
            val alpha = floor((z - 1867216.25) / 36524.25)
            z + 1 + alpha - floor(alpha / 4)
        }

        val b = a + 1524
        val c = floor((b - 122.1) / 365.25)
        val d = floor(365.25 * c)
        val e = floor((b - d) / 30.6001)
        val day = b - d - floor(30.6001 * e) + f

        val dayOfMonth = floor(day).toInt()
        val hours = (day - dayOfMonth) * 24
        val hour = floor(hours).toInt()
        val minutes = (hours - hour) * 60
        val minute = floor(minutes).toInt()
        val seconds = floor((minutes - minute) * 60).toInt()
        val month = if (e < 14) {
            e - 1
        } else {
            e - 13
        }.toInt()

        val year = if (month > 2) {
            c - 4716
        } else {
            c - 4715
        }.toInt()

        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, seconds)

    }

    fun julianCenturies(julianDay: Double): Double {
        return (julianDay - 2451545.0) / 36525.0
    }

    fun julianDateFromCenturies(julianCenturies: Double): Double {
        return julianCenturies * 36525.0 + 2451545.0
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

    /**
     * Calculates the terrestrial time
     */
    fun tt(time: ZonedDateTime): LocalDateTime {
        return ut(time).plusNanos((1e+9 * deltaT(time.year)).toLong())
    }

    fun utToLocal(ut: LocalDateTime, zone: ZoneId): ZonedDateTime {
        return ut.atZone(ZoneId.of("UTC")).withZoneSameInstant(zone)
    }

    fun ttToLocal(tt: LocalDateTime, zone: ZoneId): ZonedDateTime {
        return utToLocal(tt.minusNanos((1e+9 * deltaT(tt.year)).toLong()), zone)
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

    fun localMeanSidereal(julianDay: Double, longitude: Double): Double {
        return meanSiderealTime(julianDay) + longitude
    }

    fun localApparentSidereal(
        julianDay: Double,
        longitude: Double,
        longitudeNutation: Double,
        eclipticObliquity: Double
    ): Double {
        return apparentSiderealTime(julianDay, longitudeNutation, eclipticObliquity) + longitude
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

    fun equationOfTime(julianDay: Double): Double {
        val obliquity = obliquityCorrection(julianDay)
        val L = sunGeometricLongitude(julianDay)
        val e = eccentricity(julianDay)
        val M = sunMeanAnomaly(julianDay)
        val y = square(tanDegrees(obliquity / 2.0))

        val radTime = y * sinDegrees(2.0 * L) - 2.0 * e * sinDegrees(M) +
                4.0 * e * y * sinDegrees(M) * cosDegrees(2.0 * L) -
                0.5 * square(y) * sinDegrees(4.0 * L) -
                1.25 * square(e) * sinDegrees(2.0 * M)
        return radTime.toDegrees() * 4.0
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

    fun getSunTimes(
        date: ZonedDateTime,
        coordinate: Coordinate,
        standardAltitude: Double = -0.8333,
        withRefraction: Boolean = false
    ): RiseSetTransitTimes {
        return getTransitEvents(
            date,
            coordinate,
            standardAltitude,
            withRefraction,
            this::solarCoordinates
        )
    }

    fun getMoonTimes(
        date: ZonedDateTime,
        coordinate: Coordinate,
        standardAltitude: Double = 0.125,
        withRefraction: Boolean = false
    ): RiseSetTransitTimes {
        return getTransitEvents(
            date,
            coordinate,
            standardAltitude,
            withRefraction,
            this::lunarCoordinates
        )
    }

    fun sunMeanAnomaly(julianDay: Double): Double {
        val T = julianCenturies(julianDay)
        // TODO: Maybe don't reduce here?
        return reduceAngleDegrees(polynomial(T, 357.52911, 35999.05029, -0.0001537))
    }

    fun sunGeometricLongitude(julianDay: Double): Double {
        val T = julianCenturies(julianDay)
        return reduceAngleDegrees(polynomial(T, 280.46646, 36000.76983, 0.0003032))
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

    fun obliquityCorrection(julianDay: Double): Double {
        val T = julianCenturies(julianDay)
        val e = meanObliquityOfEcliptic(julianDay)
        val omega = polynomial(T, 125.04, -1934.136)
        return e + 0.00256 * cosDegrees(omega)
    }

    fun trueObliquityOfEcliptic(julianDay: Double): Double {
        return meanObliquityOfEcliptic(julianDay) + nutationInObliquity(julianDay)
    }

    fun eccentricity(julianDay: Double): Double {
        val T = julianCenturies(julianDay)
        return polynomial(T, 0.016708634, -0.000042037, -0.0000001267)
    }

    fun lunarCoordinates(julianDay: Double): AstroCoordinates {
        val T = (julianDay - 2451545.0) / 36525
        val L = reduceAngleDegrees(
            polynomial(
                T,
                218.3164477,
                481267.88123421,
                -0.0015786,
                1 / 538841.0,
                -1 / 65194000.0
            )
        )

        val D = reduceAngleDegrees(
            polynomial(
                T,
                297.8501921,
                445267.1114034,
                -0.0018819,
                1 / 545868.0,
                -1 / 113065000.0
            )
        )

        val M = reduceAngleDegrees(
            polynomial(
                T,
                357.5291092,
                35999.0502909,
                -0.0001536,
                1 / 24490000.0
            )
        )

        val Mprime = moonMeanAnomaly(julianDay)
        val F = moonArgumentOfLatitude(julianDay)

        val a1 = reduceAngleDegrees(119.75 + 131.849 * T)
        val a2 = reduceAngleDegrees(53.09 + 479264.290 * T)
        val a3 = reduceAngleDegrees(313.45 + 481266.484 * T)
        val E = polynomial(T, 1.0, -0.002516, -0.0000075)
        val E2 = square(E)

        val t47a = table47a()
        val t47b = table47b()

        var sumL = 0.0
        var sumB = 0.0

        for (row in t47a) {
            val eTerm = when (row[1].absoluteValue) {
                1 -> E
                2 -> E2
                else -> 1.0
            }
            sumL += row[4] * eTerm * sinDegrees(row[0] * D + row[1] * M + row[2] * Mprime + row[3] * F)
        }

        for (row in t47b) {
            val eTerm = when (row[1].absoluteValue) {
                1 -> E
                2 -> E2
                else -> 1.0
            }
            sumB += row[4] * eTerm * sinDegrees(row[0] * D + row[1] * M + row[2] * Mprime + row[3] * F)
        }

        sumL += 3958 * sinDegrees(a1) + 1962 * sinDegrees(L - F) + 318 * sinDegrees(a2)
        sumB += -2235 * sinDegrees(L) + 382 * sinDegrees(a3) + 175 * sinDegrees(a1 - F) +
                175 * sinDegrees(a1 + F) + 127 * sinDegrees(L - Mprime) - 115 * sinDegrees(L + Mprime)

        val apparentLongitude = L + sumL / 1000000.0 + nutationInLongitude(julianDay)
        val eclipticLatitude = sumB / 1000000.0
        val eclipticObliquity = trueObliquityOfEcliptic(julianDay)
        return AstroCoordinates(
            declination(apparentLongitude, eclipticObliquity, eclipticLatitude),
            rightAscension(apparentLongitude, eclipticObliquity, eclipticLatitude)
        )
    }

    fun rightAscension(
        apparentLongitude: Double,
        eclipticObliquity: Double,
        eclipticLatitude: Double
    ): Double {
        return atan2(
            sinDegrees(apparentLongitude) * cosDegrees(eclipticObliquity) - tanDegrees(
                eclipticLatitude
            ) * sinDegrees(eclipticObliquity), cosDegrees(apparentLongitude)
        ).toDegrees()
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

    private fun sunCenter(julianDay: Double): Double {
        val T = julianCenturies(julianDay)
        val M = sunMeanAnomaly(julianDay)
        // TODO: Maybe restrict to between 0 and 360
        return polynomial(T, 1.914602, -0.004817, -0.000014) * sinDegrees(M) +
                polynomial(T, 0.019993, -0.000101) * sinDegrees(2 * M) +
                0.000289 * sinDegrees(3 * M)
    }

    fun sunTrueLongitude(julianDay: Double): Double {
        val L = sunGeometricLongitude(julianDay)
        val C = sunCenter(julianDay)
        // TODO: Maybe reduce this
        return L + C
    }

    fun sunTrueAnomaly(julianDay: Double): Double {
        val M = sunMeanAnomaly(julianDay)
        val C = sunCenter(julianDay)
        // TODO: Reduce degrees?
        return M + C
    }

    fun sunRadiusVector(julianDay: Double): Double {
        val v = sunTrueAnomaly(julianDay)
        val e = eccentricity(julianDay)
        return (1.000001018 * (1 - e * e)) / (1 + e * cosDegrees(v))
    }

    fun sunApparentLongitude(julianDay: Double): Double {
        val T = julianCenturies(julianDay)
        val trueLng = sunTrueLongitude(julianDay)
        val omega = polynomial(T, 125.04, -1934.136)
        return trueLng - 0.00569 - 0.00478 * sinDegrees(omega)
    }

    fun solarCoordinates(julianDay: Double): AstroCoordinates {
        val apparentLongitude = sunApparentLongitude(julianDay)
        val correctedObliquity = obliquityCorrection(julianDay)
        val rightAscension = reduceAngleDegrees(
            atan2(
                cosDegrees(correctedObliquity) * sinDegrees(apparentLongitude),
                cosDegrees(apparentLongitude)
            ).toDegrees()
        )
        val declination = wrap(
            asin(sinDegrees(correctedObliquity) * sinDegrees(apparentLongitude)).toDegrees(),
            -90.0, 90.0
        )
        // TODO: Reduce angle or not?
        return AstroCoordinates(declination, rightAscension)
    }


//    fun parallacticAngle(hourAngle: Double, latitude: Double, declination: Double): Double {
//        return atan2(sinDegrees(hourAngle), tanDegrees(latitude) * cosDegrees(declination) - sinDegrees(declination) * cosDegrees(hourAngle)).toDegrees()
//    }

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
        val JDE = JulianDayCalculator.calculate(time.toUTCLocal()) //Julian Ephemeris Day

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

    fun getJDEOfTrueMoonPhase(k: Double): Double {
        val T = k / 1236.85
        val mean = getJDEOfMeanMoonPhase(k)
        val M = 2.5534 + 29.1053567 * k - 0.0000014 * power(T, 2) - 0.00000011 * power(T, 3)
        val MPrime = 201.5643 + 385.81693528 * k + 0.0107582 * power(T, 2) + 0.00001238 * power(
            T,
            3
        ) - 0.000000058 * power(T, 4)
        val F = 160.7108 + 390.6705084 * k - 0.0016118 * power(T, 2) - 0.0000027 * power(
            T,
            3
        ) + 0.000000011 * power(T, 4)
        val omega = 124.7746 - 1.56375588 * k + 0.0020672 * power(T, 2) + 0.00000215 * power(T, 3)
        TODO("CORRECTION")
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

    fun mercuryCoordinates(julianDay: Double): AstroCoordinates {
        return planetCoordinates(
            julianDay,
            listOf(252.250906, 149474.0722491, 0.00030350, 0.000000018),
            listOf(0.387098310),
            listOf(0.20563175, 0.000020407, -0.0000000283, -0.00000000018),
            listOf(7.004986, 0.00018215, -0.00001810, 0.000000056),
            listOf(48.330893, 1.1861883, 0.00017542, 0.000000215),
            listOf(77.456119, 1.5564776, 0.00029544, 0.000000009)
        )
    }

    fun venusCoordinates(julianDay: Double): AstroCoordinates {
        return planetCoordinates(
            julianDay,
            listOf(181.979801, 58519.2130302, 0.00031014, 0.000000015),
            listOf(0.723329820),
            listOf(0.00677192, -0.000047765, 0.0000000981, 0.00000000046),
            listOf(3.394662, 0.0010037, -0.00000088, -0.000000007),
            listOf(76.679920, 0.9011206, 0.00040618, -0.000000093),
            listOf(131.563703, 1.4022288, -0.00107618, -0.000005678)
        )
    }

    fun marsCoordinates(julianDay: Double): AstroCoordinates {
        return planetCoordinates(
            julianDay,
            listOf(355.433000, 19141.6964471, 0.00031052, 0.000000016),
            listOf(1.523679342),
            listOf(0.09340065, 0.000090484, -0.0000000806, -0.00000000025),
            listOf(1.849726, -0.0006011, 0.00001276, -0.000000007),
            listOf(49.558093, 0.7720959, 0.00001557, 0.000002267),
            listOf(336.060234, 1.8410449, 0.00013477, 0.000000536)
        )
    }

    fun jupiterCoordinates(julianDay: Double): AstroCoordinates {
        return planetCoordinates(
            julianDay,
            listOf(34.351519, 3036.3027748, 0.00022330, 0.000000037),
            listOf(5.202603209, 0.0000001913),
            listOf(0.04849793, 0.000163255, -0.0000004714, -0.00000000201),
            listOf(1.303267, -0.0054965, 0.00000466, -0.000000002),
            listOf(100.464407, 1.0209774, 0.00040315, 0.000000404),
            listOf(14.331207, 1.6126352, 0.00103042, -0.00000464)
        )
    }

    fun saturnCoordinates(julianDay: Double): AstroCoordinates {
        return planetCoordinates(
            julianDay,
            listOf(50.077444, 1223.5110686, 0.00051908, -0.000000030),
            listOf(9.554909192, -0.0000021390, 0.000000004),
            listOf(0.05554814, -0.000346641, -0.0000006436, 0.00000000340),
            listOf(2.488879, -0.0037362, -0.00001519, 0.000000087),
            listOf(113.665503, 0.8770880, -0.00012176, -0.000002249),
            listOf(93.057237, 1.9637613, 0.00083753, 0.000004928)
        )
    }

    fun uranusCoordinates(julianDay: Double): AstroCoordinates {
        return planetCoordinates(
            julianDay,
            listOf(314.055005, 429.8640561, 0.00030390, 0.000000026),
            listOf(19.218446062, -0.0000000372, 0.00000000098),
            listOf(0.04638122, -0.000027293, 0.0000000789, 0.00000000024),
            listOf(0.773197, 0.0007744, 0.00003749, -0.000000092),
            listOf(74.005957, 0.5211278, 0.00133947, 0.000018484),
            listOf(173.005291, 1.4863790, 0.00021406, 0.000000434)
        )
    }

    fun neptuneCoordinates(julianDay: Double): AstroCoordinates {
        return planetCoordinates(
            julianDay,
            listOf(304.348665, 219.8333092, 0.00030882, 0.000000018),
            listOf(30.110386869, -0.0000001663, 0.00000000069),
            listOf(0.00945575, 0.000006033, 0.0, -0.00000000005),
            listOf(1.769953, -0.0093082, -0.00000708, 0.000000027),
            listOf(131.784057, 1.1022039, 0.00025952, -0.000000637),
            listOf(48.120276, 1.4262957, 0.00038434, 0.000000020)
        )
    }

    fun planetCoordinates(
        julianDay: Double,
        meanLongitude: List<Double>,
        semimajorAxis: List<Double>,
        eccentricity: List<Double>,
        inclination: List<Double>,
        ascendingNodeLongitude: List<Double>,
        perihelionLongitude: List<Double>,
        includesSpeedOfLight: Boolean = false
    ): AstroCoordinates {
        val t = julianCenturies(julianDay)
        val l = reduceAngleDegrees(polynomial(t, *meanLongitude.toDoubleArray()))
        val a = polynomial(t, *semimajorAxis.toDoubleArray())
        val e = polynomial(t, *eccentricity.toDoubleArray())
        val i = wrap(polynomial(t, *inclination.toDoubleArray()), 0.0, 180.0)
        val omega = reduceAngleDegrees(polynomial(t, *ascendingNodeLongitude.toDoubleArray()))
        val pi = reduceAngleDegrees(polynomial(t, *perihelionLongitude.toDoubleArray()))
        val m = reduceAngleDegrees(l - pi)
        val w = pi - omega

        val eclipticObliquity = meanObliquityOfEcliptic(julianDay)

        val F = cosDegrees(omega)
        val G = sinDegrees(omega) * cosDegrees(eclipticObliquity)
        val H = sinDegrees(omega) * sinDegrees(eclipticObliquity)
        val P = -sinDegrees(omega) * cosDegrees(i)
        val Q =
            cosDegrees(omega) * cosDegrees(i) * cosDegrees(eclipticObliquity) - sinDegrees(i) * sinDegrees(
                eclipticObliquity
            )
        val R =
            cosDegrees(omega) * cosDegrees(i) * sinDegrees(eclipticObliquity) + sinDegrees(i) * cosDegrees(
                eclipticObliquity
            )


        val A = atan2(F, P).toDegrees()
        val B = atan2(G, Q).toDegrees()
        val C = atan2(H, R).toDegrees()

        val a2 = sqrt(F * F + P * P)
        val b2 = sqrt(G * G + Q * Q)
        val c2 = sqrt(H * H + R * R)

        var E = m
        for (iter in 0..10) {
            E += (m + e.toDegrees() * sinDegrees(E) - E) / (1 - e * cosDegrees(E))
        }
        E = reduceAngleDegrees(E)
        val v =
            reduceAngleDegrees(2 * atan(sqrt((1 + e) / (1 - e)) * tanDegrees(E / 2)).toDegrees())
        val r = a * (1 - e * cosDegrees(E))

        val x = r * a2 * sinDegrees(A + w + v)
        val y = r * b2 * sinDegrees(B + w + v)
        val z = r * c2 * sinDegrees(C + w + v)

        val sunLongitude = sunGeometricLongitude(julianDay)
        val meanObliquity = meanObliquityOfEcliptic(julianDay)
        val sunRadius = sunRadiusVector(julianDay)

        val X = sunRadius * cosDegrees(sunLongitude)
        val Y = sunRadius * (sinDegrees(sunLongitude) * cosDegrees(meanObliquity))
        val Z = sunRadius * (sinDegrees(sunLongitude) * sinDegrees(meanObliquity))

        val xDiff = X + x
        val yDiff = Y + y
        val zDiff = Z + z

        if (!includesSpeedOfLight) {
            val dist = Vector3(xDiff.toFloat(), yDiff.toFloat(), zDiff.toFloat()).magnitude()
            val speedOfLight = 299792458.0
            val secondsBefore = dist / speedOfLight
            return planetCoordinates(
                julianDay - secondsBefore,
                meanLongitude,
                semimajorAxis,
                eccentricity,
                inclination,
                ascendingNodeLongitude,
                perihelionLongitude,
                true
            )
        }

        val ascension = reduceAngleDegrees(atan2(yDiff, xDiff).toDegrees())
        val declination =
            wrap(atan2(zDiff, sqrt(xDiff * xDiff + yDiff * yDiff)).toDegrees(), -90.0, 90.0)

        return AstroCoordinates(declination, ascension)
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

    private fun cube(a: Double): Double {
        return a * a * a
    }

    private fun square(a: Double): Double {
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

    private fun table54_1(): List<List<Int>> {
        return listOf(
            // [term, E bool, M, Mprime, F prime, A prime, omega]
            listOf(-4065, 0, 0, 1, 0, 0, 0),
            listOf(1727, 1, 1, 0, 0, 0, 0),
            listOf(161, 0, 0, 2, 0, 0, 0),
            listOf(-97, 0, 0, 0, 2, 0, 0),
            listOf(73, 1, -1, 1, 0, 0, 0),
            listOf(-50, 1, 1, 1, 0, 0, 0),
            listOf(-23, 0, 0, 1, -2, 0, 0),
            listOf(21, 1, 2, 0, 0, 0, 0),
            listOf(12, 0, 0, 1, 2, 0, 0),
            listOf(6, 1, 1, 2, 0, 0, 0),
            listOf(-4, 0, 0, 3, 0, 0, 0),
            listOf(-3, 1, 1, 0, 2, 0, 0),
            listOf(3, 0, 0, 0, 0, 1, 0),
            listOf(-2, 1, 1, 0, -2, 0, 0),
            listOf(-2, 1, -1, 2, 0, 0, 0),
            listOf(-2, 0, 0, 0, 0, 0, 1),
        )
    }

    private fun table54_P(): List<List<Int>> {
        return listOf(
            // [term, E bool, M, Mprime, F prime]
            listOf(2070, 1, 1, 0, 0),
            listOf(24, 1, 2, 0, 0),
            listOf(-392, 0, 0, 1, 0),
            listOf(116, 0, 0, 2, 0),
            listOf(-73, 1, 1, 1, 0),
            listOf(67, 1, -1, 1, 0),
            listOf(118, 0, 0, 0, 2),
        )
    }

    private fun table54_Q(): List<List<Int>> {
        return listOf(
            // [term, E bool, M, Mprime]
            listOf(52207, 0, 0, 0),
            listOf(-48, 1, 1, 0),
            listOf(20, 1, 2, 0),
            listOf(-3299, 0, 0, 1),
            listOf(-60, 1, 1, 1),
            listOf(41, 1, -1, 1),
        )
    }

    private fun table47a(): List<List<Int>> {
        return listOf(
            listOf(0, 0, 1, 0, 6288774, -20905355),
            listOf(2, 0, -1, 0, 1274027, -3699111),
            listOf(2, 0, 0, 0, 658314, -2955968),
            listOf(0, 0, 2, 0, 213618, -569925),
            listOf(0, 1, 0, 0, -185116, 48888),
            listOf(0, 0, 0, 2, -114332, -3149),
            listOf(2, 0, -2, 0, 58793, 246158),
            listOf(2, -1, -1, 0, 57066, -152138),
            listOf(2, 0, 1, 0, 53322, -170733),
            listOf(2, -1, 0, 0, 45758, -204586),
            listOf(0, 1, -1, 0, -40923, -129620),
            listOf(1, 0, 0, 0, -34720, 108743),
            listOf(0, 1, 1, 0, -30383, 104755),
            listOf(2, 0, 0, -2, 15327, 10321),
            listOf(0, 0, 1, 2, -12528, 0),
            listOf(0, 0, 1, -2, 10980, 79661),
            listOf(4, 0, -1, 0, 10675, -34782),
            listOf(0, 0, 3, 0, 10034, -23210),
            listOf(4, 0, -2, 0, 8548, -21636),
            listOf(2, 1, -1, 0, -7888, 24208),
            listOf(2, 1, 0, 0, -6766, 30824),
            listOf(1, 0, -1, 0, -5163, -8379),
            listOf(1, 1, 0, 0, 4987, -16675),
            listOf(2, -1, 1, 0, 4036, -12831),
            listOf(2, 0, 2, 0, 3994, -10445),
            listOf(4, 0, 0, 0, 3861, -11650),
            listOf(2, 0, -3, 0, 3665, 14403),
            listOf(0, 1, -2, 0, -2689, -7003),
            listOf(2, 0, -1, 2, -2602, 0),
            listOf(2, -1, -2, 0, 2390, 10056),
            listOf(1, 0, 1, 0, -2348, 6322),
            listOf(2, -2, 0, 0, 2236, -9884),
            listOf(0, 1, 2, 0, -2120, 5751),
            listOf(0, 2, 0, 0, -2069, 0),
            listOf(2, -2, -1, 0, 2048, -4950),
            listOf(2, 0, 1, -2, -1773, 4130),
            listOf(2, 0, 0, 2, -1595, 0),
            listOf(4, -1, -1, 0, 1215, -3958),
            listOf(0, 0, 2, 2, -1110, 0),
            listOf(3, 0, -1, 0, -892, 3258),
            listOf(2, 1, 1, 0, -810, 2616),
            listOf(4, -1, -2, 0, 759, -1897),
            listOf(0, 2, -1, 0, -713, -2117),
            listOf(2, 2, -1, 0, -700, 2354),
            listOf(2, 1, -2, 0, 691, 0),
            listOf(2, -1, 0, -2, 596, 0),
            listOf(4, 0, 1, 0, 549, -1423),
            listOf(0, 0, 4, 0, 537, -1117),
            listOf(4, -1, 0, 0, 520, -1571),
            listOf(1, 0, -2, 0, -487, -1739),
            listOf(2, 1, 0, -2, -399, 0),
            listOf(0, 0, 2, -2, -381, -4421),
            listOf(1, 1, 1, 0, 351, 0),
            listOf(3, 0, -2, 0, -340, 0),
            listOf(4, 0, -3, 0, 330, 0),
            listOf(2, -1, 2, 0, 327, 0),
            listOf(0, 2, 1, 0, -323, 1165),
            listOf(1, 1, -1, 0, 299, 0),
            listOf(2, 0, 3, 0, 294, 0),
            listOf(2, 0, -1, -2, 0, 8752)
        )
    }

    private fun table47b(): List<List<Int>> {
        return listOf(
            listOf(0, 0, 0, 1, 5128122),
            listOf(0, 0, 1, 1, 280602),
            listOf(0, 0, 1, -1, 277693),
            listOf(2, 0, 0, -1, 173237),
            listOf(2, 0, -1, 1, 55413),
            listOf(2, 0, -1, -1, 46271),
            listOf(2, 0, 0, 1, 32573),
            listOf(0, 0, 2, 1, 17198),
            listOf(2, 0, 1, -1, 9266),
            listOf(0, 0, 2, -1, 8822),
            listOf(2, -1, 0, -1, 8216),
            listOf(2, 0, -2, -1, 4324),
            listOf(2, 0, 1, 1, 4200),
            listOf(2, 1, 0, -1, -3359),
            listOf(2, -1, -1, 1, 2463),
            listOf(2, -1, 0, 1, 2211),
            listOf(2, -1, -1, -1, 2065),
            listOf(0, 1, -1, -1, -1870),
            listOf(4, 0, -1, -1, 1828),
            listOf(0, 1, 0, 1, -1794),
            listOf(0, 0, 0, 3, -1749),
            listOf(0, 1, -1, 1, -1565),
            listOf(1, 0, 0, 1, -1491),
            listOf(0, 1, 1, 1, -1475),
            listOf(0, 1, 1, -1, -1410),
            listOf(0, 1, 0, -1, -1344),
            listOf(1, 0, 0, -1, -1335),
            listOf(0, 0, 3, 1, 1107),
            listOf(4, 0, 0, -1, 1021),
            listOf(4, 0, -1, 1, 833),
            listOf(0, 0, 1, -3, 777),
            listOf(4, 0, -2, 1, 671),
            listOf(2, 0, 0, -3, 607),
            listOf(2, 0, 2, -1, 596),
            listOf(2, -1, 1, -1, 491),
            listOf(2, 0, -2, 1, -451),
            listOf(0, 0, 3, -1, 439),
            listOf(2, 0, 2, 1, 422),
            listOf(2, 0, -3, -1, 421),
            listOf(2, 1, -1, 1, -366),
            listOf(2, 1, 0, 1, -351),
            listOf(4, 0, 0, 1, 331),
            listOf(2, -1, 1, 1, 315),
            listOf(2, -2, 0, -1, 302),
            listOf(0, 0, 1, 3, -283),
            listOf(2, 1, 1, -1, -229),
            listOf(1, 1, 0, -1, 223),
            listOf(1, 1, 0, 1, 223),
            listOf(0, 1, -2, -1, -220),
            listOf(2, 1, -1, -1, -220),
            listOf(1, 0, 1, 1, -185),
            listOf(2, -1, -2, -1, 181),
            listOf(0, 1, 2, 1, -177),
            listOf(4, 0, -2, -1, 176),
            listOf(4, -1, -1, -1, 166),
            listOf(1, 0, 1, -1, -164),
            listOf(4, 0, 1, -1, 132),
            listOf(1, 0, -1, -1, -119),
            listOf(4, -1, 0, -1, 115),
            listOf(2, -2, 0, 1, 107)
        )
    }
}


data class AstroCoordinates(val declination: Double, val rightAscension: Double)
