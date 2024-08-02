package com.kylecorry.sol.science.oceanography

internal object AstronomicalArgumentCalculator {

    // TODO: Calculate like pytide https://github.com/sam-cox/pytides/blob/master/pytides/constituent.py

    fun get(constituent: TideConstituent, year: Int): Float {
        // From https://www.dfo-mpo.gc.ca/science/data-donnees/tidal-marees/argument-u-v-eng.html
        return get2022(constituent)
    }

    private fun get2022(constituent: TideConstituent): Float {
        val Q1 = 45.003f
        val O1 = 44.031f
        val P1 = 348.805f
        val K1 = 3.577f
        val N2 = 46.36f
        val M2 = 45.013f
        val S2 = 0.113f
        val K2 = 186.65f

        // Other
        val L2 = 213.7788f

        return when (constituent) {
            TideConstituent.M2 -> M2
            TideConstituent.S2 -> S2
            TideConstituent.N2 -> N2
            TideConstituent.K1 -> K1
            TideConstituent.M4 -> M2 * 2
            TideConstituent.O1 -> O1
            TideConstituent.M6 -> M2 * 3
            TideConstituent.MK3 -> M2 + K1
            TideConstituent.S4 -> S2 * 2
            TideConstituent.MN4 -> M2 + N2
            TideConstituent.NU2 -> M2
            TideConstituent.S6 -> S2 * 3
            TideConstituent.MU2 -> M2 * 2 - S2
            TideConstituent._2N2 -> M2
            TideConstituent.LAM2 -> M2
            TideConstituent.S1 -> 0f
            TideConstituent.SSA -> 0f
            TideConstituent.SA -> 0f
            TideConstituent.MSF -> S2 - M2
            TideConstituent.RHO -> M2 - K1 // NU2 - K1
            TideConstituent.Q1 -> Q1
            TideConstituent.T2 -> 0f
            TideConstituent.R2 -> 0f
            TideConstituent.P1 -> P1
            TideConstituent._2SM2 -> 2 * S2 - M2
            TideConstituent.L2 -> L2
            TideConstituent._2MK3 -> M2 + O1
            TideConstituent.K2 -> K2
            TideConstituent.M8 -> M2 * 4
            TideConstituent.MS4 -> M2 + S2
            TideConstituent.Z0 -> 0f
        }
    }

}