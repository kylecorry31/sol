package com.kylecorry.sol.science.astronomy.corrections

import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.polynomial
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.science.astronomy.units.toJulianCenturies

internal object EclipticObliquity {
    fun getTrueObliquityOfEcliptic(ut: UniversalTime): Double {
        return getMeanObliquityOfEcliptic(ut) + getNutationInObliquity(ut)
    }

    fun getMeanObliquityOfEcliptic(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        return getMeanObliquityOfEcliptic(T)
    }

    fun getMeanObliquityOfEcliptic(T: Double): Double {
        val u = T / 100.0
        return polynomial(
            u,
            84381.448,
            -4680.93,
            -1.55,
            1999.25,
            -51.38,
            -249.67,
            -39.05,
            7.12,
            27.87,
            5.79,
            2.45
        ) / 3600.0
    }

    private fun getNutationInObliquity(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        val X0 = polynomial(T, 297.85036, 445267.111480, -0.0019142, 1 / 189474.0)
        val X1 = polynomial(T, 357.52772, 35999.050340, -0.0001603, -1 / 300000.0)
        val X2 = polynomial(T, 134.96298, 477198.867398, 0.0086972, 1 / 56250.0)
        val X3 = polynomial(T, 93.27191, 483202.017538, -0.0036825, 1 / 327270.0)
        val X4 = polynomial(T, 125.04452, -1934.136261, 0.0020708, 1 / 450000.0)

        var nutation = 0.0
        val table = LongitudinalNutation.table3()
        for (row in table) {
            nutation += (row[7] + row[8] * T) * cosDegrees(row[0] * X0 + row[1] * X1 + row[2] * X2 + row[3] * X3 + row[4] * X4)
        }
        return nutation / 36000000.0
    }

}