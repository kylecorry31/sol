package com.kylecorry.sol.science.astronomy.sun

import com.kylecorry.sol.math.TSMath.cosDegrees
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.science.astronomy.AstroUtils
import com.kylecorry.sol.science.astronomy.locators.Sun
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import kotlin.math.pow

internal class SolarRadiationCalculator {

    private val sun = Sun()

    /**
     * Gets the solar radiation in kW/m^2 at the given time and location
     */
    fun getRadiation(ut: UniversalTime, location: Coordinate): Double {
        val altitude = AstroUtils.getAltitude(sun, ut, location, false).toDouble()
        if (altitude < 0) {
            return 0.0
        }
        val am = 1 / cosDegrees(90 - altitude)
        val radiantPowerDensity = (1 + 0.033 * cosDegrees(360 * (ut.dayOfYear - 2) / 365.0)) * 1.353
        return radiantPowerDensity * 0.7.pow(am.pow(0.678))
    }

}