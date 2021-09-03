package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.andromeda.core.math.*
import com.kylecorry.andromeda.core.time.plusHours
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.locators.ICelestialLocator
import com.kylecorry.trailsensecore.domain.astronomy.units.*
import com.kylecorry.trailsensecore.domain.math.MathUtils
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin

// TODO: Remove the duplication in this class
class RiseSetTransitTimeCalculator {

    fun calculate(
        locator: ICelestialLocator,
        date: ZonedDateTime,
        location: Coordinate,
        standardAltitude: Double = 0.0,
        withRefraction: Boolean = false
    ): RiseSetTransitTimes {

        return getTransitEvents(
            date,
            location,
            standardAltitude,
            withRefraction
        ) {
            locator.getCoordinates(it)
        }
    }

    private fun getTransitEvents(
        date: ZonedDateTime,
        coordinate: Coordinate,
        standardAltitude: Double,
        withRefraction: Boolean,
        coordinateFn: (ut: UniversalTime) -> EquatorialCoordinate
    ): RiseSetTransitTimes {

        val ld = date.toLocalDate()

        // Get today's times
        val today =
            getTransitTimesHelper(
                date,
                coordinate,
                standardAltitude,
                withRefraction,
                coordinateFn
            )
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

    private fun getTransitTimesHelper(
        date: ZonedDateTime,
        coordinate: Coordinate,
        standardAltitude: Double,
        withRefraction: Boolean,
        coordinateFn: (ut: UniversalTime) -> EquatorialCoordinate
    ): RiseSetTransitTimes {
        val ut = Astro.ut0hOnDate(date)
        val uty = Astro.ut0hOnDate(date.minusDays(1))
        val utt = Astro.ut0hOnDate(date.plusDays(1))
        val longitudeNutation = nutationInLongitude(ut)
        val eclipticObliquity = trueObliquityOfEcliptic(ut)
        val sr = apparentSiderealTime(ut, longitudeNutation, eclipticObliquity)
        val astroCoords = coordinateFn.invoke(ut)
        val astroCoordsy = coordinateFn.invoke(uty)
        val astroCoordst = coordinateFn.invoke(utt)
        val times = riseSetTransitTimes(
            coordinate.latitude,
            coordinate.longitude,
            sr,
            standardAltitude,
            withRefraction,
            Astro.deltaT(date.year),
            Triple(astroCoordsy.declination, astroCoords.declination, astroCoordst.declination),
            Triple(
                astroCoordsy.rightAscension,
                astroCoords.rightAscension,
                astroCoordst.rightAscension
            )
        )
            ?: return RiseSetTransitTimes(null, null, null)

        val rise = Astro.utToLocal(ut.plusHours(times.first), date.zone)
        val transit = Astro.utToLocal(ut.plusHours(times.second), date.zone)
        val set = Astro.utToLocal(ut.plusHours(times.third), date.zone)

        return RiseSetTransitTimes(rise, transit, set)
    }

    private fun apparentSiderealTime(
        ut: UniversalTime,
        longitudeNutation: Double,
        eclipticObliquity: Double
    ): Double {
        val meanSidereal = meanSiderealTime(ut)
        return meanSidereal + (longitudeNutation * cosDegrees(eclipticObliquity)) / 15.0
    }

    private fun meanSiderealTime(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        val theta0 =
            280.46061837 + 360.98564736629 * (ut.toJulianDay() - 2451545.0) + 0.000387933 * Astro.square(
                T
            ) - Astro.cube(
                T
            ) / 38710000.0
        return wrap(theta0, 0.0, 360.0)
    }

    private fun trueObliquityOfEcliptic(ut: UniversalTime): Double {
        return meanObliquityOfEcliptic(ut) + nutationInObliquity(ut)
    }

    private fun nutationInObliquity(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        val L = 280.4665 + 36000.7698 * T
        val LPrime = 218.3165 + 481267.8813 * T
        val omega = moonAscendingNodeLongitude(ut)
        return 0.002555556 * cosDegrees(omega) + 0.0001583333 * cosDegrees(2 * L) +
                0.00002777778 * cosDegrees(2 * LPrime) - 0.000025 * cosDegrees(2 * omega)
    }

    private fun meanObliquityOfEcliptic(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        val seconds = MathUtils.polynomial(T, 21.448, -46.815, -0.00059, 0.001813)
        return 23.0 + (26.0 + seconds / 60.0) / 60.0
    }


    private fun nutationInLongitude(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        val L = 280.4665 + 36000.7698 * T
        val LPrime = 218.3165 + 481267.8813 * T
        val omega = moonAscendingNodeLongitude(ut)
        return -0.004777778 * sinDegrees(omega) + 0.0003666667 * sinDegrees(2 * L) -
                0.00006388889 * sinDegrees(2 * LPrime) + 0.00005833333 * sinDegrees(2 * omega)
    }

    private fun moonAscendingNodeLongitude(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        return MathUtils.polynomial(T, 125.04452, -1934.136261, 0.0020708, 1 / 450000.0)
    }

    private fun riseSetTransitTimes(
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
            val sidereal0 = Astro.reduceAngleDegrees(apparentSidereal + 360.985647 * m0)
            val sidereal1 = Astro.reduceAngleDegrees(apparentSidereal + 360.985647 * m1)
            val sidereal2 = Astro.reduceAngleDegrees(apparentSidereal + 360.985647 * m2)

            val n0 = m0 + deltaT / 86400
            val n1 = m1 + deltaT / 86400
            val n2 = m2 + deltaT / 86400

            val normalizedRas = normalizeRightAscensions(rightAscensions)

            val ra0 =
                Astro.reduceAngleDegrees(
                    Astro.interpolate(
                        n0,
                        normalizedRas.first,
                        normalizedRas.second,
                        normalizedRas.third
                    )
                )
            val ra1 =
                Astro.reduceAngleDegrees(
                    Astro.interpolate(
                        n1,
                        normalizedRas.first,
                        normalizedRas.second,
                        normalizedRas.third
                    )
                )
            val ra2 = Astro.reduceAngleDegrees(
                Astro.interpolate(
                    n2,
                    normalizedRas.first,
                    normalizedRas.second,
                    normalizedRas.third
                )
            )
            val declination1 =
                Astro.interpolate(n1, declinations.first, declinations.second, declinations.third)
            val declination2 =
                Astro.interpolate(n2, declinations.first, declinations.second, declinations.third)

            val hourAngle0 = Astro.reduceAngleDegrees(hourAngle(sidereal0, longitude, ra0))
            val hourAngle1 = Astro.reduceAngleDegrees(hourAngle(sidereal1, longitude, ra1))
            val hourAngle2 = Astro.reduceAngleDegrees(hourAngle(sidereal2, longitude, ra2))

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

    private fun hourAngle(sidereal: Double, longitude: Double, rightAscension: Double): Double {
        return sidereal + longitude - rightAscension
    }

    private fun altitude(
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

    private fun refraction(elevation: Double): Double {
        if (elevation > 85.0) {
            return 0.0
        }

        val tanElev = tanDegrees(elevation)

        if (elevation > 5.0) {
            return (58.1 / tanElev - 0.07 / Astro.cube(tanElev) + 0.000086 / power(
                tanElev,
                5
            )) / 3600.0
        }

        if (elevation > -0.575) {
            return MathUtils.polynomial(elevation, 1735.0, -518.2, 103.4, -12.79, 0.711) / 3600.0
        }

        return -20.774 / tanElev / 3600.0
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

}