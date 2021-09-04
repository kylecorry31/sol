package com.kylecorry.trailsensecore.domain.astronomy.corrections

import com.kylecorry.andromeda.core.math.sinDegrees
import com.kylecorry.trailsensecore.domain.astronomy.units.UniversalTime
import com.kylecorry.trailsensecore.domain.astronomy.units.toJulianCenturies
import com.kylecorry.trailsensecore.domain.math.MathUtils

object LongitudinalNutation {

    fun getNutationInLongitude(ut: UniversalTime): Double {
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

}