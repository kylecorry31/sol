package com.kylecorry.sol.science.oceanography

internal object AstronomicalArgumentCalculator {

    // TODO: Calculate like pytide https://github.com/sam-cox/pytides/blob/master/pytides/constituent.py

    fun get(constituent: TideConstituent, year: Int): Float {
        // From https://www.dfo-mpo.gc.ca/science/data-donnees/tidal-marees/argument-u-v-eng.html
        return get2022(constituent)
    }

    private fun get2022(constituent: TideConstituent): Float {
        return when (constituent) {
            TideConstituent.M2 -> 45.013f
            TideConstituent.S2 -> 0.113f
            TideConstituent.N2 -> 46.36f
            TideConstituent.K1 -> 3.577f
            TideConstituent.M4 -> 45.013f * 2
            TideConstituent.O1 -> 44.031f
            TideConstituent.M6 -> 3 * 45.013f
            TideConstituent.MK3 -> 45.013f + 3.577f // M2 + K1
            TideConstituent.S4 -> 0.113f * 2 // S2 + S2
            TideConstituent.MN4 -> 45.013f + 46.36f // M2 + N2
            TideConstituent.NU2 -> 45.013f // M2
            TideConstituent.S6 -> 0.113f * 3 // S2 + S2 + S2
            TideConstituent.MU2 -> 45.013f * 2 - 0.113f // M2 * 2 - S2
            TideConstituent._2N2 -> 45.013f // M2
            TideConstituent.LAM2 -> 45.013f // M2
            TideConstituent.S1 -> 0f
            TideConstituent.SSA -> 0f
            TideConstituent.SA -> 0f
            TideConstituent.MSF -> 0.113f - 45.013f // S2 - M2
            TideConstituent.RHO -> 45.013f - 3.577f // NU2 - K1
            TideConstituent.Q1 -> 45.003f
            TideConstituent.T2 -> 0f
            TideConstituent.R2 -> 0f
            TideConstituent.P1 -> 348.805f
            TideConstituent._2SM2 -> 2 * 0.113f - 45.013f // 2 * S2 - M2
            TideConstituent.L2 -> 213.7788f // Calculated
            TideConstituent._2MK3 -> 45.013f + 44.031f // M2 + O1
            TideConstituent.K2 -> 186.65f
            TideConstituent.M8 -> 45.013f * 4 // M2 * 4
            TideConstituent.MS4 -> 45.013f + 0.113f // M2 + S2
            TideConstituent.Z0 -> 0f
        }
    }

}