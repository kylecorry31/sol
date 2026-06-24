package com.kylecorry.sol.science.astronomy.eclipse.lunar

import com.kylecorry.sol.math.trigonometry.Trigonometry.normalizeAngle
import com.kylecorry.sol.science.astronomy.eclipse.LunarEclipseShadow
import com.kylecorry.sol.science.astronomy.locators.Moon
import com.kylecorry.sol.science.astronomy.locators.Sun
import com.kylecorry.sol.science.astronomy.units.CelestialObservation
import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.units.HorizonCoordinate
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import java.time.ZonedDateTime

internal class LunarEclipseShadowCalculator {

    private val sun = Sun()
    private val moon = Moon()
    private val provider = LunarEclipseParameterProvider()

    fun getPeakShadow(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): LunarEclipseShadow {
        val ut = time.toUniversalTime()
        val sunCoordinates = sun.getCoordinates(ut)
        val shadowCoordinates = getShadowCoordinates(sunCoordinates)
        val moonDistance = moon.getDistance(ut)
        val shadowPosition = getShadowPosition(
            shadowCoordinates,
            time,
            location,
            withRefraction,
            withParallax,
            moonDistance
        )

        val parameters = provider.getNextLunarEclipseParameters(time.minusDays(20).toInstant())
        val moonDiameter = Moon.DIAMETER_IN_EARTH_RADII
        val umbraRadius =
            LUNAR_ECLIPSE_UMBRA_OUTER_CONTACT_RADIUS -
                parameters.umbralConeRadius -
                Moon.RADIUS_IN_EARTH_RADII
        val penumbraRadius =
            LUNAR_ECLIPSE_PENUMBRA_OUTER_CONTACT_RADIUS +
                parameters.umbralConeRadius -
                Moon.RADIUS_IN_EARTH_RADII
        val umbraDiameter = (2 * umbraRadius).coerceAtLeast(0.0)
        val penumbraDiameter = (2 * penumbraRadius).coerceAtLeast(0.0)
        val moonAngularDiameter = if (withParallax) {
            moon.getAngularDiameter(ut, location, moonDistance)
        } else {
            moon.getAngularDiameter(ut)
        }.toFloat()
        val umbraAngularDiameter = umbraDiameter / moonDiameter * moonAngularDiameter
        val penumbraAngularDiameter = penumbraDiameter / moonDiameter * moonAngularDiameter

        return LunarEclipseShadow(
            umbra = getShadowObservation(shadowPosition, umbraAngularDiameter.toFloat()),
            penumbra = getShadowObservation(shadowPosition, penumbraAngularDiameter.toFloat())
        )
    }

    private fun getShadowPosition(
        shadowCoordinates: EquatorialCoordinate,
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean,
        withParallax: Boolean,
        distanceToMoon: Distance
    ): HorizonCoordinate {
        val ut = time.toUniversalTime()
        val position = if (withParallax) {
            HorizonCoordinate.fromEquatorial(shadowCoordinates, ut, location, distanceToMoon)
        } else {
            HorizonCoordinate.fromEquatorial(shadowCoordinates, ut, location)
        }

        return if (withRefraction) {
            position.withRefraction()
        } else {
            position
        }
    }

    private fun getShadowCoordinates(sunCoordinates: EquatorialCoordinate): EquatorialCoordinate {
        return EquatorialCoordinate(
            -sunCoordinates.declination,
            normalizeAngle(sunCoordinates.rightAscension + 180)
        )
    }

    private fun getShadowObservation(
        position: HorizonCoordinate,
        angularDiameter: Float
    ): CelestialObservation {
        return CelestialObservation(
            Bearing.from(position.azimuth.toFloat()),
            position.altitude.toFloat(),
            angularDiameter
        )
    }
}
