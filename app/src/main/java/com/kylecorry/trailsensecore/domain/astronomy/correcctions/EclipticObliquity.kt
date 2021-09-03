package com.kylecorry.trailsensecore.domain.astronomy.correcctions

import com.kylecorry.andromeda.core.math.cosDegrees
import com.kylecorry.trailsensecore.domain.astronomy.units.UniversalTime
import com.kylecorry.trailsensecore.domain.astronomy.units.toJulianCenturies
import com.kylecorry.trailsensecore.domain.math.MathUtils

object EclipticObliquity {
    fun getTrueObliquityOfEcliptic(ut: UniversalTime): Double {
        return getMeanObliquityOfEcliptic(ut) + getNutationInObliquity(ut)
    }

    fun getMeanObliquityOfEcliptic(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        val seconds = MathUtils.polynomial(T, 21.448, -46.815, -0.00059, 0.001813)
        return 23.0 + (26.0 + seconds / 60.0) / 60.0
    }

    private fun getNutationInObliquity(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        val L = 280.4665 + 36000.7698 * T
        val LPrime = 218.3165 + 481267.8813 * T
        val omega = getAscendingNodeLongitude(ut)
        return 0.002555556 * cosDegrees(omega) + 0.0001583333 * cosDegrees(2 * L) +
                0.00002777778 * cosDegrees(2 * LPrime) - 0.000025 * cosDegrees(2 * omega)
    }

    private fun getAscendingNodeLongitude(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        return MathUtils.polynomial(T, 125.04452, -1934.136261, 0.0020708, 1 / 450000.0)
    }
}