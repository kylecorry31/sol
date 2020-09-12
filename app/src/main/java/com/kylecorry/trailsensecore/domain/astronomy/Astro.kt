package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.trailsensecore.domain.Coordinate
import com.kylecorry.trailsensecore.domain.math.*
import com.kylecorry.trailsensecore.domain.time.plusHours
import java.time.*
import kotlin.math.*

internal object Astro {

    fun timeToAngle(hours: Number, minutes: Number, seconds: Number): Double {
        return timeToDecimal(hours, minutes, seconds) * 15
    }

    fun timeToDecimal(hours: Number, minutes: Number, seconds: Number): Double {
        return hours.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
    }

    fun degreesToHours(degrees: Double): Double {
        return degrees / 15.0
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

    fun interpolate(n: Double, y1: Double, y2: Double, y3: Double): Double {
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

    fun altitude(hourAngle: Double, latitude: Double, declination: Double): Double {
        return wrap(
            Math.toDegrees(
                asin(
                    sinDegrees(latitude) * sinDegrees(declination) + cosDegrees(
                        latitude
                    ) * cosDegrees(declination) * cosDegrees(hourAngle)
                )
            ), -90.0, 90.0
        )
    }

    fun riseSetTransitTimes(
        latitude: Double,
        longitude: Double,
        apparentSidereal: Double,
        standardAltitude: Double,
        declination: Double,
        rightAscension: Double
    ): NTuple5<Double, Double, Double, Boolean, Boolean> {
        val cosH =
            (sinDegrees(standardAltitude) - sinDegrees(latitude) * sinDegrees(declination)) / (cosDegrees(
                latitude
            ) * cosDegrees(declination))

        if (cosH >= 1) {
            // Always down
            return NTuple5(0.0, 0.0, 0.0, false, true)
        } else if (cosH <= -1) {
            // Always up
            return NTuple5(0.0, 0.0, 0.0, true, false)
        }

        val H = wrap(Math.toDegrees(acos(cosH)), 0.0, 180.0)

        val m0 = wrap((rightAscension - longitude - apparentSidereal) / 360.0, 0.0, 1.0)
        val m1 = wrap(m0 - H / 360, 0.0, 1.0)
        val m2 = wrap(m0 + H / 360, 0.0, 1.0)

        val riseHour = m1 * 24
        val transitHour = m0 * 24
        val setHour = m2 * 24

        return NTuple5(riseHour, transitHour, setHour, false, false)
    }

    fun accurateRiseSetTransitTimes(
        latitude: Double,
        longitude: Double,
        apparentSidereal: Double,
        standardAltitude: Double,
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
            return Triple(-1.0, -1.0, -1.0)
        } else if (cosH <= -1) {
            // Always up
            return Triple(-1.0, 0.0, -1.0)
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

            val ra0 = interpolate(
                n0,
                rightAscensions.first,
                rightAscensions.second,
                rightAscensions.third
            )
            val ra1 = interpolate(
                n1,
                rightAscensions.first,
                rightAscensions.second,
                rightAscensions.third
            )
            val ra2 = interpolate(
                n2,
                rightAscensions.first,
                rightAscensions.second,
                rightAscensions.third
            )
            val declination1 =
                interpolate(n1, declinations.first, declinations.second, declinations.third)
            val declination2 =
                interpolate(n2, declinations.first, declinations.second, declinations.third)

            val hourAngle0 = wrap(hourAngle(sidereal0, longitude, ra0), -180.0, 180.0)
            val hourAngle1 = wrap(hourAngle(sidereal1, longitude, ra1), -180.0, 180.0)
            val hourAngle2 = wrap(hourAngle(sidereal2, longitude, ra2), -180.0, 180.0)

            val altitude1 = wrap(altitude(hourAngle1, latitude, declination1), -180.0, 180.0)
            val altitude2 = wrap(altitude(hourAngle2, latitude, declination2), -180.0, 180.0)

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

    fun getTransitEvents(
        date: ZonedDateTime,
        coordinate: Coordinate,
        standardAltitude: Double,
        coordinateFn: (julianDate: Double) -> AstroCoordinates,
        initialDate: ZonedDateTime
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
        val times = accurateRiseSetTransitTimes(
            coordinate.latitude,
            coordinate.longitude,
            sr,
            standardAltitude,
            deltaT(date.year),
            Triple(astroCoordsy.declination, astroCoords.declination, astroCoordst.declination),
            Triple(
                astroCoordsy.rightAscension,
                astroCoords.rightAscension,
                astroCoordst.rightAscension
            )
        )
            ?: return RiseSetTransitTimes(null, null, null)

//        if (times.fourth) {
//            return RiseSetTransitTimes(null, null, null)
//        }
//
//        if (times.fifth) {
//            return RiseSetTransitTimes(null, null, null)
//        }

        var rise = utToLocal(ut.plusHours(times.first), date.zone)
        var transit = utToLocal(ut.plusHours(times.second), date.zone)
        var set = utToLocal(ut.plusHours(times.third), date.zone)

        // TODO: This needs to be revisited
        if (transit.toLocalDate().isBefore(initialDate.toLocalDate())) {
            transit = getTransitEvents(
                date.plusDays(1),
                coordinate,
                standardAltitude,
                coordinateFn,
                initialDate
            ).transit!!
        } else if (transit.toLocalDate().isAfter(initialDate.toLocalDate())) {
            transit = getTransitEvents(
                date.minusDays(1),
                coordinate,
                standardAltitude,
                coordinateFn,
                initialDate
            ).transit!!
        }

        if (rise.toLocalDate().isBefore(initialDate.toLocalDate())) {
            rise = getTransitEvents(
                date.plusDays(1),
                coordinate,
                standardAltitude,
                coordinateFn,
                initialDate
            ).rise!!
        } else if (rise.toLocalDate().isAfter(initialDate.toLocalDate())) {
            rise = getTransitEvents(
                date.minusDays(1),
                coordinate,
                standardAltitude,
                coordinateFn,
                initialDate
            ).rise!!
        }

        if (set.toLocalDate().isBefore(initialDate.toLocalDate())) {
            set = getTransitEvents(
                date.plusDays(1),
                coordinate,
                standardAltitude,
                coordinateFn,
                initialDate
            ).set!!
        } else if (set.toLocalDate().isAfter(initialDate.toLocalDate())) {
            set = getTransitEvents(
                date.minusDays(1),
                coordinate,
                standardAltitude,
                coordinateFn,
                initialDate
            ).set!!
        }

        return RiseSetTransitTimes(rise, transit, set)
    }

    fun getSunTimes(
        date: ZonedDateTime,
        coordinate: Coordinate,
        standardAltitude: Double = -0.8333
    ): RiseSetTransitTimes {
        return getTransitEvents(date, coordinate, standardAltitude, this::solarCoordinates, date)
    }

    fun getMoonTimes(
        date: ZonedDateTime,
        coordinate: Coordinate,
        standardAltitude: Double = 0.125
    ): RiseSetTransitTimes {
        return getTransitEvents(date, coordinate, standardAltitude, this::lunarCoordinates, date)
    }

    fun sunMeanAnomaly(julianDay: Double): Double {
        val T = (julianDay - 2451545.0) / 36525
        return reduceAngleDegrees(polynomial(T, 357.52772, 35999.05340, -0.0001603, -1 / 3000000.0))
    }

    fun sunGeometricLongitude(julianDay: Double): Double {
        val T = (julianDay - 2451545.0) / 36525
        return polynomial(T, 280.46646, 36000.76983, 0.0003032)
    }

    fun moonMeanAnomaly(julianDay: Double): Double {
        val T = (julianDay - 2451545.0) / 36525
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

    fun sunToMoonMeanElongation(julianDay: Double): Double {
        val T = (julianDay - 2451545.0) / 36525
        return polynomial(T, 297.85036, 445267.111480, -0.0019142, 1 / 189474.0)
    }

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
        val T = (julianDay - 2451545.0) / 36525
        return polynomial(T, 23.43929, -0.01300417, -1.638889e-7, 5.036111e-7)
    }

    fun trueObliquityOfEcliptic(julianDay: Double): Double {
        return meanObliquityOfEcliptic(julianDay) + nutationInObliquity(julianDay)
    }

    fun eccentricity(julianDay: Double): Double {
        val T = (julianDay - 2451545.0) / 36525
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

    fun solarCoordinates(julianDay: Double): AstroCoordinates {
        val T = (julianDay - 2451545.0) / 36525
        val M = sunMeanAnomaly(julianDay)
        val L = sunGeometricLongitude(julianDay)
        val C = polynomial(T, 1.914602, -0.004817, -0.000014) * sinDegrees(M) +
                polynomial(T, 0.019993, -0.000101) * sinDegrees(2 * M) +
                0.000289 * sinDegrees(3 * M)
        val trueLng = reduceAngleDegrees(L + C)
        val sunAscendingNodeLongitude = 125.04 - 1934.136 * T
        val apparentLongitude = trueLng - 0.00569 - 0.00478 * sinDegrees(sunAscendingNodeLongitude)
        val obliquity = meanObliquityOfEcliptic(julianDay)
        val correctedObliquity = obliquity + 0.00256 * cosDegrees(sunAscendingNodeLongitude)
        val rightAscension = atan2(
            cosDegrees(correctedObliquity) * sinDegrees(apparentLongitude),
            cosDegrees(apparentLongitude)
        ).toDegrees()
        val declination =
            asin(sinDegrees(correctedObliquity) * sinDegrees(apparentLongitude)).toDegrees()

        return AstroCoordinates(declination, rightAscension)
    }


//    fun parallacticAngle(hourAngle: Double, latitude: Double, declination: Double): Double {
//        return atan2(sinDegrees(hourAngle), tanDegrees(latitude) * cosDegrees(declination) - sinDegrees(declination) * cosDegrees(hourAngle)).toDegrees()
//    }

    fun hourToDuration(hours: Double): Duration {
        return Duration
            .ofHours(hours.toLong())
            .plusMinutes(((hours % 1) * 60).toLong())
            .plusSeconds(((hours * 60) % 1).toLong())
    }

    fun hourToLocalTime(hours: Double): LocalTime {
        return LocalTime.of(hours.toInt(), ((hours % 1) * 60).toInt(), ((hours * 60) % 1).toInt())
    }


    private fun wrap(value: Double, min: Double, max: Double): Double {
        val range = max - min

        var newValue = value

        while (newValue > max) {
            newValue -= range
        }

        while (newValue < min) {
            newValue += range
        }

        return newValue
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

data class NTuple5<T1, T2, T3, T4, T5>(
    val first: T1,
    val second: T2,
    val third: T3,
    val fourth: T4,
    val fifth: T5
)
