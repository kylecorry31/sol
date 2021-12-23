package com.kylecorry.sol.science.oceanography

import com.kylecorry.sol.math.SolMath.square

internal object NodeFactorCalculator {
    // TODO: Use Schureman equations to calculate (https://github.com/sam-cox/pytides/blob/master/pytides/nodal_corrections.py)

    fun get(constituent: TideConstituent, year: Int): Float {
        return if (year <= 2021) {
            get2021(constituent)
        } else {
            get2022(constituent)
        }
    }

    private fun get2021(constituent: TideConstituent): Float {
        return when (constituent) {
            TideConstituent.M2 -> 0.986f
            TideConstituent.S2 -> 1.001f
            TideConstituent.N2 -> 0.984f
            TideConstituent.K1 -> 1.053f
            TideConstituent.M4 -> square(0.986f)
            TideConstituent.O1 -> 1.088f
            TideConstituent.P1 -> 0.997f
            TideConstituent.L2 -> 0.8537f
            TideConstituent.K2 -> 1.125f
            TideConstituent.MS4 -> square(0.986f) * square(1.001f)
            TideConstituent.Z0 -> 1f
        }
    }

    private fun get2022(constituent: TideConstituent): Float {
        return when (constituent) {
            TideConstituent.M2 -> 0.975f
            TideConstituent.S2 -> 1.001f
            TideConstituent.N2 -> 0.978f
            TideConstituent.K1 -> 1.081f
            TideConstituent.M4 -> square(0.975f)
            TideConstituent.O1 -> 1.140f
            TideConstituent.P1 -> 0.995f
            TideConstituent.L2 -> 1.2437f
            TideConstituent.K2 -> 1.214f
            TideConstituent.MS4 -> square(0.975f) * square(1.001f)
            TideConstituent.Z0 -> 1f
        }
    }

}