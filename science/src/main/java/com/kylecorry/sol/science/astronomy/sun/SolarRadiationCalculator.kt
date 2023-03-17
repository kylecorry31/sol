package com.kylecorry.sol.science.astronomy.sun

import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.science.astronomy.AstroUtils
import com.kylecorry.sol.science.astronomy.locators.Sun
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.units.Bearing
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
        val altitude =
            AstroUtils.getAltitude(sun, ut, location, withRefraction, withParallax).toDouble()
        if (altitude < 0) {
            return 0.0
        }
        val am = 1 / cosDegrees(90 - altitude)
        val radiantPowerDensity = (1 + 0.033 * cosDegrees(360 * (ut.dayOfYear - 2) / 365.0)) * 1.353
        val incident = radiantPowerDensity * 0.7.pow(am.pow(0.678))

        if (tilt == null || bearing == null) {
            return incident
        }

        val azimuth = AstroUtils.getAzimuth(sun, ut, location, withParallax).value.toDouble()

        return incident * (
                cosDegrees(altitude) * sinDegrees(tilt) * cosDegrees(bearing.value - azimuth) + sinDegrees(
                    altitude
                ) * cosDegrees(tilt)
                )

    }

}