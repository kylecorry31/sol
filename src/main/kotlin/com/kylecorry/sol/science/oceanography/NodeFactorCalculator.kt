package com.kylecorry.sol.science.oceanography

import com.kylecorry.sol.math.SolMath.cube
import com.kylecorry.sol.math.SolMath.power
import com.kylecorry.sol.math.SolMath.square

internal object NodeFactorCalculator {
    // TODO: Use Schureman equations to calculate (https://github.com/sam-cox/pytides/blob/master/pytides/nodal_corrections.py)

    fun get(constituent: TideConstituent, year: Int): Float {
        // From https://www.dfo-mpo.gc.ca/science/data-donnees/tidal-marees/facteur-node-factor-eng.html
        return get2022(constituent)
    }

    private fun get2022(constituent: TideConstituent): Float {
        return when (constituent) {
            TideConstituent.M2 -> 0.975f
            TideConstituent.S2 -> 1.001f
            TideConstituent.N2 -> 0.978f
            TideConstituent.K1 -> 1.081f
            TideConstituent.M4 -> square(0.975f)
            TideConstituent.O1 -> 1.140f
            TideConstituent.M6 -> cube(0.975f)
            TideConstituent.MK3 -> 0.975f * 1.081f // M2 * K1
            TideConstituent.S4 -> square(1.001f) // S2 * S2
            TideConstituent.MN4 -> 0.975f * 0.978f // M2 * N2
            TideConstituent.NU2 -> 0.975f // M2
            TideConstituent.S6 -> cube(1.001f) // S2 * S2 * S2
            TideConstituent.MU2 -> square(0.975f) * 1.001f // M2 * M2 * S2
            TideConstituent._2N2 -> 0.975f // M2
            TideConstituent.LAM2 -> 0.975f // M2
            TideConstituent.S1 -> 1f
            TideConstituent.SSA -> 1f
            TideConstituent.SA -> 1f
            TideConstituent.MSF -> 1.001f * 0.975f // S2 * M2
            TideConstituent.RHO -> 0.975f * 1.081f // NU2 * K1
            TideConstituent.Q1 -> 1.102f
            TideConstituent.T2 -> 1f
            TideConstituent.R2 -> 1f
            TideConstituent.P1 -> 0.995f
            TideConstituent._2SM2 -> square(1.001f) * 0.975f // 2 * S2 * M2
            TideConstituent.L2 -> 1.2437f
            TideConstituent._2MK3 -> 0.975f * 1.140f // M2 * O1
            TideConstituent.K2 -> 1.214f
            TideConstituent.M8 -> power(0.975f, 4) // M2 * M2 * M2 * M2
            TideConstituent.MS4 -> 0.975f * 1.001f
            TideConstituent.Z0 -> 1f
        }
    }

}