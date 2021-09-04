package com.kylecorry.trailsensecore.science.astronomy

import com.kylecorry.trailsensecore.units.Coordinate
import com.kylecorry.trailsensecore.science.astronomy.corrections.EclipticObliquity
import com.kylecorry.trailsensecore.science.astronomy.corrections.LongitudinalNutation
import com.kylecorry.trailsensecore.science.astronomy.corrections.TerrestrialTime
import com.kylecorry.trailsensecore.science.astronomy.locators.ICelestialLocator
import com.kylecorry.trailsensecore.science.astronomy.units.*
import com.kylecorry.trailsensecore.math.TSMath
import com.kylecorry.trailsensecore.math.TSMath.cosDegrees
import com.kylecorry.trailsensecore.math.TSMath.sinDegrees
import com.kylecorry.trailsensecore.math.TSMath.wrap
import com.kylecorry.trailsensecore.time.TimeUtils
import com.kylecorry.trailsensecore.time.TimeUtils.plusHours
import com.kylecorry.trailsensecore.time.TimeUtils.toLocal
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.acos

internal class RiseSetTransitTimeCalculator {

    fun calculate(
        locator: ICelestialLocator,
        date: ZonedDateTime,
        location: Coordinate,
        standardAltitude: Double = 0.0,
        withRefraction: Boolean = false
    ): RiseSetTransitTimes {

        val ld = date.toLocalDate()

        // Get today's times
        val today =
            getTransitTimesHelper(
                date,
                location,
                standardAltitude,
                withRefraction,
                locator
            )
        if (today.rise?.toLocalDate() == ld && today.transit?.toLocalDate() == ld && today.set?.toLocalDate() == ld) {
            return today
        }

        // Today's times didn't contain all the events / were on the wrong day, check the surrounding days
        val yesterday =
            getTransitTimesHelper(
                date.minusDays(1),
                location,
                standardAltitude,
                withRefraction,
                locator
            )
        val tomorrow =
            getTransitTimesHelper(
                date.plusDays(1),
                location,
                standardAltitude,
                withRefraction,
                locator
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
        locator: ICelestialLocator
    ): RiseSetTransitTimes {
        val ut = TimeUtils.ut0hOnDate(date)
        val uty = TimeUtils.ut0hOnDate(date.minusDays(1))
        val utt = TimeUtils.ut0hOnDate(date.plusDays(1))
        val astroCoords = locator.getCoordinates(ut)
        val astroCoordsy = locator.getCoordinates(uty)
        val astroCoordst = locator.getCoordinates(utt)
        val times = getRiseSetTransitTimes(
            ut,
            coordinate,
            standardAltitude,
            withRefraction,
            Triple(astroCoordsy, astroCoords, astroCoordst)
        )
            ?: return RiseSetTransitTimes(null, null, null)

        val rise = ut.plusHours(times.first).toLocal(date.zone)
        val transit = ut.plusHours(times.second).toLocal(date.zone)
        val set = ut.plusHours(times.third).toLocal(date.zone)

        return RiseSetTransitTimes(rise, transit, set)
    }

    private fun getApparentSiderealTime(ut: UniversalTime): Double {
        val longitudeNutation = LongitudinalNutation.getNutationInLongitude(ut)
        val eclipticObliquity = EclipticObliquity.getTrueObliquityOfEcliptic(ut)
        val meanSidereal = getMeanSiderealTime(ut)
        return meanSidereal + (longitudeNutation * cosDegrees(eclipticObliquity)) / 15.0
    }

    private fun getMeanSiderealTime(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        val theta0 =
            280.46061837 + 360.98564736629 * (ut.toJulianDay() - 2451545.0) + 0.000387933 * TSMath.square(
                T
            ) - TSMath.cube(
                T
            ) / 38710000.0
        return wrap(theta0, 0.0, 360.0)
    }

    private fun getRiseSetTransitTimes(
        ut: UniversalTime,
        location: Coordinate,
        standardAltitude: Double,
        withRefraction: Boolean,
        coordinates: Triple<EquatorialCoordinate, EquatorialCoordinate, EquatorialCoordinate>
    ): Triple<Double, Double, Double>? {
        val apparentSidereal = getApparentSiderealTime(ut)
        val deltaT = TerrestrialTime.getDeltaT(ut.year)
        val cosH =
            (sinDegrees(standardAltitude) - sinDegrees(location.latitude) * sinDegrees(coordinates.second.declination)) / (cosDegrees(
                location.latitude
            ) * cosDegrees(coordinates.second.declination))

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

        var m0 = wrap(
            (coordinates.second.rightAscension - location.longitude - apparentSidereal) / 360.0,
            0.0,
            1.0
        )
        var m1 = wrap(m0 - H / 360, 0.0, 1.0)
        var m2 = wrap(m0 + H / 360, 0.0, 1.0)
        val date = ut.toLocalDate()

        for (i in 0 until iterations) {
            val sidereal0 =
                GreenwichSiderealTime(TSMath.reduceAngleDegrees(apparentSidereal + 360.985647 * m0) / 15)
            val sidereal1 =
                GreenwichSiderealTime(TSMath.reduceAngleDegrees(apparentSidereal + 360.985647 * m1) / 15)
            val sidereal2 =
                GreenwichSiderealTime(TSMath.reduceAngleDegrees(apparentSidereal + 360.985647 * m2) / 15)

            val n0 = m0 + deltaT / 86400
            val n1 = m1 + deltaT / 86400
            val n2 = m2 + deltaT / 86400

            val c0 =
                interpolateCoordinate(n0, coordinates.first, coordinates.second, coordinates.third)
            val c1 =
                interpolateCoordinate(n1, coordinates.first, coordinates.second, coordinates.third)
            val c2 =
                interpolateCoordinate(n2, coordinates.first, coordinates.second, coordinates.third)

            val hourAngle0 = c0.getHourAngle(sidereal0.atLongitude(location.longitude)) * 15
            val hourAngle1 = c1.getHourAngle(sidereal1.atLongitude(location.longitude)) * 15
            val hourAngle2 = c2.getHourAngle(sidereal2.atLongitude(location.longitude)) * 15

            val altitude1 =
                AstroUtils.getAltitude(
                    c1,
                    sidereal1.toUniversalTime(date),
                    location,
                    withRefraction
                )
            val altitude2 =
                AstroUtils.getAltitude(
                    c2,
                    sidereal2.toUniversalTime(date),
                    location,
                    withRefraction
                )

            val dm0 = -hourAngle0 / 360
            val dm1 =
                (altitude1 - standardAltitude) / (360 * cosDegrees(c1.declination) * cosDegrees(
                    location.latitude
                ) * sinDegrees(hourAngle1))
            val dm2 =
                (altitude2 - standardAltitude) / (360 * cosDegrees(c2.declination) * cosDegrees(
                    location.latitude
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


    private fun interpolateCoordinate(
        value: Double,
        first: EquatorialCoordinate,
        second: EquatorialCoordinate,
        third: EquatorialCoordinate
    ): EquatorialCoordinate {
        val normalizedRas = normalizeRightAscensions(
            Triple(
                first.rightAscension,
                second.rightAscension,
                third.rightAscension
            )
        )

        val ra = TSMath.interpolate(
            value,
            normalizedRas.first,
            normalizedRas.second,
            normalizedRas.third
        )

        val declination =
            TSMath.interpolate(value, first.declination, second.declination, third.declination)

        return EquatorialCoordinate(declination, ra)
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