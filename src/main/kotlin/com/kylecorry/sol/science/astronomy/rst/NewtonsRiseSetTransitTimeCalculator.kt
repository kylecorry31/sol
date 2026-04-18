package com.kylecorry.sol.science.astronomy.rst


import com.kylecorry.sol.math.MathExtensions.toDegrees
import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.arithmetic.Arithmetic.wrap
import com.kylecorry.sol.math.interpolation.Interpolation
import com.kylecorry.sol.math.trigonometry.Trigonometry
import com.kylecorry.sol.math.trigonometry.Trigonometry.cosDegrees
import com.kylecorry.sol.math.trigonometry.Trigonometry.sinDegrees
import com.kylecorry.sol.science.astronomy.AstroUtils
import com.kylecorry.sol.science.astronomy.RiseSetTransitTimes
import com.kylecorry.sol.science.astronomy.corrections.EclipticObliquity
import com.kylecorry.sol.science.astronomy.corrections.LongitudinalNutation
import com.kylecorry.sol.science.astronomy.corrections.TerrestrialTime
import com.kylecorry.sol.science.astronomy.locators.ICelestialLocator
import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.units.GreenwichSiderealTime
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.science.astronomy.units.toJulianCenturies
import com.kylecorry.sol.science.astronomy.units.toJulianDay
import com.kylecorry.sol.science.astronomy.units.toLocal
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.science.astronomy.units.ut0hOnDate
import com.kylecorry.sol.time.Time.plusHours
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.acos

internal class NewtonsRiseSetTransitTimeCalculator : IRiseSetTransitTimeCalculator {

    override fun calculate(
        locator: ICelestialLocator,
        date: ZonedDateTime,
        location: Coordinate,
        standardAltitude: Double,
        withRefraction: Boolean,
        withParallax: Boolean
    ): RiseSetTransitTimes {
        val maxAttempts = if (withRefraction) 2 else 1
        var rise: ZonedDateTime? = null
        var transit: ZonedDateTime? = null
        var set: ZonedDateTime? = null

        for (attempt in 0..<maxAttempts) {
            val currentWithRefraction = if (attempt == 0) withRefraction else false
            val calculated = calculateHelper(
                locator,
                date,
                location,
                standardAltitude,
                currentWithRefraction,
                withParallax
            )

            rise = rise ?: calculated.rise
            transit = transit ?: calculated.transit
            set = set ?: calculated.set

            if (rise != null && transit != null && set != null) {
                break
            }
        }

        return RiseSetTransitTimes(rise, transit, set)
    }

    private fun calculateHelper(
        locator: ICelestialLocator,
        date: ZonedDateTime,
        location: Coordinate,
        standardAltitude: Double,
        withRefraction: Boolean,
        withParallax: Boolean
    ): RiseSetTransitTimes {
        val ld = date.toLocalDate()

        // Get today's times
        val today =
            getTransitTimesHelper(
                date,
                location,
                standardAltitude,
                withRefraction,
                withParallax,
                locator
            )
        if (today.rise?.toLocalDate() == ld && today.transit?.toLocalDate() == ld && today.set?.toLocalDate() == ld) {
            return today
        }

        // Get today's times (at noon) - this is needed around DST changes in the UK
        // (I'm not 100% sure why - seems to occur when getting the UT 0)
        val todayAtNoon = getTransitTimesHelper(
            date.withHour(12).withMinute(0).withSecond(0).withNano(0),
            location,
            standardAltitude,
            withRefraction,
            withParallax,
            locator
        )

        // Today's times didn't contain all the events / were on the wrong day, check the surrounding days
        val yesterday =
            getTransitTimesHelper(
                date.minusDays(1),
                location,
                standardAltitude,
                withRefraction,
                withParallax,
                locator
            )
        val tomorrow =
            getTransitTimesHelper(
                date.plusDays(1),
                location,
                standardAltitude,
                withRefraction,
                withParallax,
                locator
            )

        return RiseSetTransitTimes(
            listOfNotNull(
                today.rise,
                todayAtNoon.rise,
                yesterday.rise,
                tomorrow.rise
            ).firstOrNull { it.toLocalDate() == ld },
            listOfNotNull(
                today.transit,
                todayAtNoon.transit,
                yesterday.transit,
                tomorrow.transit
            ).firstOrNull { it.toLocalDate() == ld },
            listOfNotNull(
                today.set,
                todayAtNoon.set,
                yesterday.set,
                tomorrow.set
            ).firstOrNull { it.toLocalDate() == ld }
        )
    }

    private fun getTransitTimesHelper(
        date: ZonedDateTime,
        coordinate: Coordinate,
        standardAltitude: Double,
        withRefraction: Boolean,
        withParallax: Boolean,
        locator: ICelestialLocator
    ): RiseSetTransitTimes {
        val ut = ut0hOnDate(date)
        val utYesterday = ut0hOnDate(date.minusDays(1))
        val utTomorrow = ut0hOnDate(date.plusDays(1))
        val astroCoords = locator.getCoordinates(ut)
        val astroCoordsYesterday = locator.getCoordinates(utYesterday)
        val astroCoordsTomorrow = locator.getCoordinates(utTomorrow)
        val distance = if (withParallax) locator.getDistance(ut) else null
        val distanceYesterday = if (withParallax) locator.getDistance(utYesterday) else null
        val distanceTomorrow = if (withParallax) locator.getDistance(utTomorrow) else null
        val times = getRiseSetTransitTimes(
            ut,
            coordinate,
            standardAltitude,
            withRefraction,
            Triple(astroCoordsYesterday, astroCoords, astroCoordsTomorrow),
            if (distance != null && distanceYesterday != null && distanceTomorrow != null) Triple(
                distance,
                distanceYesterday,
                distanceTomorrow
            ) else null
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
        val julianCenturiesSinceJ2000 = ut.toJulianCenturies()
        val theta0 =
            280.46061837 + 360.98564736629 * (ut.toJulianDay() - 2451545.0) + 0.000387933 * Arithmetic.square(
                julianCenturiesSinceJ2000
            ) - Arithmetic.cube(
                julianCenturiesSinceJ2000
            ) / 38710000.0
        return wrap(theta0, 0.0, 360.0)
    }

    private fun getRiseSetTransitTimes(
        ut: UniversalTime,
        location: Coordinate,
        standardAltitude: Double,
        withRefraction: Boolean,
        coordinates: Triple<EquatorialCoordinate, EquatorialCoordinate, EquatorialCoordinate>,
        distances: Triple<Distance, Distance, Distance>?
    ): Triple<Double, Double, Double>? {
        val apparentSidereal = getApparentSiderealTime(ut)
        val deltaT = TerrestrialTime.getDeltaT(ut.year)
        val hourAngle = getHourAngle(standardAltitude, location, coordinates.second) ?: return null
        check(hourAngle in 0.0..180.0) { "Hour angle is invalid" }

        var transitDayFraction = wrap(
            (coordinates.second.rightAscension - location.longitude - apparentSidereal) / 360.0,
            0.0,
            1.0
        )
        var riseDayFraction = wrap(transitDayFraction - hourAngle / 360, 0.0, 1.0)
        var setDayFraction = wrap(transitDayFraction + hourAngle / 360, 0.0, 1.0)
        val date = ut.toLocalDate()

        for (ignored in 0..<MAX_ITERATIONS) {
            val transitDayFractionAdjustment = getTransitAdjustment(
                apparentSidereal,
                transitDayFraction,
                deltaT,
                coordinates,
                location
            )

            val riseDayFractionAdjustment = getRiseSetAdjustment(
                apparentSidereal,
                riseDayFraction,
                deltaT,
                coordinates,
                distances,
                date,
                location,
                withRefraction,
                standardAltitude,
            )
            val setDayFractionAdjustment = getRiseSetAdjustment(
                apparentSidereal,
                setDayFraction,
                deltaT,
                coordinates,
                distances,
                date,
                location,
                withRefraction,
                standardAltitude,
            )

            transitDayFraction = wrap(transitDayFraction + transitDayFractionAdjustment, 0.0, 1.0)
            riseDayFraction = wrap(riseDayFraction + riseDayFractionAdjustment, 0.0, 1.0)
            setDayFraction = wrap(setDayFraction + setDayFractionAdjustment, 0.0, 1.0)

            if (
                abs(transitDayFractionAdjustment) < DONE_THRESHOLD &&
                abs(riseDayFractionAdjustment) < DONE_THRESHOLD &&
                abs(setDayFractionAdjustment) < DONE_THRESHOLD
            ) {
                break
            }
        }

        val riseHour = riseDayFraction * 24
        val transitHour = transitDayFraction * 24
        val setHour = setDayFraction * 24

        return Triple(riseHour, transitHour, setHour)
    }

    private fun getGreenwichSiderealTime(
        apparentSiderealDegrees: Double,
        dayFraction: Double
    ): GreenwichSiderealTime {
        require(dayFraction in 0.0..1.0) { "Day fraction must be between 0 and 1" }
        val siderealDegreesAtTime =
            Trigonometry.normalizeAngle(apparentSiderealDegrees + 360.985647 * dayFraction)
        val siderealHoursAtTime = siderealDegreesAtTime / 15
        return GreenwichSiderealTime(siderealHoursAtTime)
    }

    private fun getHourAngle(
        standardAltitudeDegrees: Double,
        location: Coordinate,
        coordinate: EquatorialCoordinate
    ): Double? {
        val cosineHourAngle =
            (sinDegrees(standardAltitudeDegrees) - sinDegrees(location.latitude) * sinDegrees(
                coordinate.declination
            )) / (cosDegrees(location.latitude) * cosDegrees(coordinate.declination))

        if (cosineHourAngle >= 1) {
            // Always down
            return null
        } else if (cosineHourAngle <= -1) {
            // Always up
            return null
        }

        return wrap(acos(cosineHourAngle).toDegrees(), 0.0, 180.0)
    }

    private fun getTerrestrialDayFraction(
        universalDayFraction: Double,
        deltaTSeconds: Double
    ): Double {
        return universalDayFraction + deltaTSeconds / 86400
    }

    private fun getTransitAdjustment(
        apparentSiderealDegrees: Double,
        dayFraction: Double,
        deltaTSeconds: Double,
        coordinates: Triple<EquatorialCoordinate, EquatorialCoordinate, EquatorialCoordinate>,
        location: Coordinate
    ): Double {
        val siderealTime = getGreenwichSiderealTime(apparentSiderealDegrees, dayFraction)
        val terrestrialDayFraction = getTerrestrialDayFraction(dayFraction, deltaTSeconds)
        val interpolatedCoordinate = interpolateCoordinate(
            terrestrialDayFraction,
            coordinates.first,
            coordinates.second,
            coordinates.third
        )
        val hourAngleDegrees =
            interpolatedCoordinate.getHourAngle(siderealTime.atLongitude(location.longitude)) * 15
        return -hourAngleDegrees / 360
    }

    private fun getRiseSetAdjustment(
        apparentSiderealDegrees: Double,
        dayFraction: Double,
        deltaTSeconds: Double,
        coordinates: Triple<EquatorialCoordinate, EquatorialCoordinate, EquatorialCoordinate>,
        distances: Triple<Distance, Distance, Distance>?,
        date: java.time.LocalDate,
        location: Coordinate,
        withRefraction: Boolean,
        standardAltitudeDegrees: Double,
    ): Double {
        val siderealTime = getGreenwichSiderealTime(apparentSiderealDegrees, dayFraction)
        val terrestrialDayFraction = getTerrestrialDayFraction(dayFraction, deltaTSeconds)
        val interpolatedCoordinate = interpolateCoordinate(
            terrestrialDayFraction,
            coordinates.first,
            coordinates.second,
            coordinates.third
        )
        val interpolatedDistance =
            distances?.let {
                interpolateDistance(terrestrialDayFraction, it.first, it.second, it.third)
            }
        val hourAngleDegrees =
            interpolatedCoordinate.getHourAngle(siderealTime.atLongitude(location.longitude)) * 15
        val altitudeDegrees =
            AstroUtils.getAltitude(
                interpolatedCoordinate,
                siderealTime.toUniversalTime(date),
                location,
                withRefraction,
                interpolatedDistance
            )
        return getRiseSetAltitudeAdjustment(
            altitudeDegrees,
            standardAltitudeDegrees,
            interpolatedCoordinate.declination,
            location.latitude,
            hourAngleDegrees
        )
    }

    private fun getRiseSetAltitudeAdjustment(
        altitudeDegrees: Float,
        standardAltitudeDegrees: Double,
        declinationDegrees: Double,
        latitudeDegrees: Double,
        hourAngleDegrees: Double
    ): Double {
        val denominator =
            360 * cosDegrees(declinationDegrees) * cosDegrees(latitudeDegrees) * sinDegrees(
                hourAngleDegrees
            )
        return (altitudeDegrees - standardAltitudeDegrees) / denominator
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

        val ra = Interpolation.catmullRom(
            value,
            normalizedRas.first,
            normalizedRas.second,
            normalizedRas.third
        )

        val declination =
            Interpolation.catmullRom(value, first.declination, second.declination, third.declination)

        return EquatorialCoordinate(declination, ra)
    }

    private fun interpolateDistance(
        value: Double,
        first: Distance,
        second: Distance,
        third: Distance
    ): Distance {
        val distance = Interpolation.catmullRom(
            value,
            first.value.toDouble(),
            second.value.toDouble(),
            third.value.toDouble()
        )
        return Distance.from(distance.toFloat(), first.units)
    }

    private fun normalizeRightAscensions(
        rightAscensions: Triple<Double, Double, Double>
    ): Triple<Double, Double, Double> {
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

    companion object {
        private const val MAX_ITERATIONS = 20
        private const val DONE_THRESHOLD = 0.0001
    }

}
