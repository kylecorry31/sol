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
        val Q1 = 1.102f
        val O1 = 1.140f
        val P1 = 0.995f
        val K1 = 1.081f
        val N2 = 0.978f
        val M2 = 0.975f
        val S2 = 1.001f
        val K2 = 1.214f

        // Other
        val L2 = 1.2437f

        return when (constituent) {
            TideConstituent.M2 -> M2
            TideConstituent.S2 -> S2
            TideConstituent.N2 -> N2
            TideConstituent.K1 -> K1
            TideConstituent.M4 -> square(M2)
            TideConstituent.O1 -> O1
            TideConstituent.M6 -> cube(M2)
            TideConstituent.MK3 -> M2 * K1
            TideConstituent.S4 -> square(S2)
            TideConstituent.MN4 -> M2 * N2
            TideConstituent.NU2 -> M2
            TideConstituent.S6 -> cube(S2)
            TideConstituent.MU2 -> square(M2) * S2
            TideConstituent._2N2 -> M2
            TideConstituent.LAM2 -> M2
            TideConstituent.S1 -> 1f
            TideConstituent.SSA -> 1f
            TideConstituent.SA -> 1f
            TideConstituent.MSF -> S2 * M2
            TideConstituent.RHO -> M2 * K1 // NU2 * K1
            TideConstituent.Q1 -> Q1
            TideConstituent.T2 -> 1f
            TideConstituent.R2 -> 1f
            TideConstituent.P1 -> P1
            TideConstituent._2SM2 -> square(S2) * M2
            TideConstituent.L2 -> L2
            TideConstituent._2MK3 -> M2 * O1
            TideConstituent.K2 -> K2
            TideConstituent.M8 -> power(M2, 4)
            TideConstituent.MS4 -> M2 * S2
            TideConstituent.Z0 -> 1f
        }
    }

}