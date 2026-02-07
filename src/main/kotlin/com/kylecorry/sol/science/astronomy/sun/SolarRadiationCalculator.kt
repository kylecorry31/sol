package com.kylecorry.sol.science.astronomy.sun

import com.kylecorry.sol.math.trigonometry.Trigonometry.cosDegrees
import com.kylecorry.sol.math.trigonometry.Trigonometry.sinDegrees
import com.kylecorry.sol.science.astronomy.AstroUtils
import com.kylecorry.sol.science.astronomy.locators.Sun
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import kotlin.math.pow

internal class SolarRadiationCalculator {

    private val sun = Sun()

    /**
     * Gets the solar radiation in kW/m^2 at the given time and location
     */
    fun getRadiation(
        ut: UniversalTime,
        location: Coordinate,
        tilt: Float? = null,
        bearing: Bearing? = null,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Double {
        val coords = AstroUtils.getLocation(sun, ut, location, withRefraction, withParallax)
        val altitude = coords.altitude
        if (altitude < 0) {
            return 0.0
        }
        val am = 1 / cosDegrees(90 - altitude)
        val radiantPowerDensity = (1 + 0.033 * cosDegrees(360 * (ut.dayOfYear - 2) / 365.0)) * 1.353
        val incident = radiantPowerDensity * 0.7.pow(am.pow(0.678))

        if (tilt == null || bearing == null) {
            return incident
        }

        val azimuth = coords.azimuth

        return incident * (cosDegrees(altitude) * sinDegrees(tilt) * cosDegrees(bearing.value - azimuth) + sinDegrees(
            altitude
        ) * cosDegrees(tilt))

    }

}