package com.kylecorry.sol.science.oceanography

internal object AstronomicalArgumentCalculator {

    // TODO: Calculate like pytide https://github.com/sam-cox/pytides/blob/master/pytides/constituent.py

    fun get(constituent: TideConstituent, year: Int): Float {
        // From https://www.dfo-mpo.gc.ca/science/data-donnees/tidal-marees/argument-u-v-eng.html
        return get2022(constituent)
    }

    private fun get2022(constituent: TideConstituent): Float {
        val Q1 = 46.68567641428672f
        val O1 = 45.41488367971033f
        val P1 = 349.36365099256363f
        val K1 = 2.9652328633674188f
        val N2 = 46.203216556459665f
        val M2 = 44.932423821883276f
        val S2 = 0.0f
        val K2 = 186.0166575879848f
        val L2 = 223.6616310874815f
        val M1 = 22.466211910941638f
        val J1 = 358.21227030735463f
        val MM = 358.72920726554f
        val MF = 134.10265646269545f
        val M3 = 247.3986357327085f

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
            TideConstituent.M1 -> M1
            TideConstituent.J1 -> J1
            TideConstituent.MM -> MM
            TideConstituent.SSA -> 0f
            TideConstituent.SA -> 0f
            TideConstituent.MSF -> S2 - M2
            TideConstituent.MF -> MF
            TideConstituent.RHO -> M2 - K1 // NU2 - K1
            TideConstituent.Q1 -> Q1
            TideConstituent.T2 -> 0f
            TideConstituent.R2 -> 0f
            TideConstituent._2Q1 -> N2 - J1
            TideConstituent.P1 -> P1
            TideConstituent._2SM2 -> 2 * S2 - M2
            TideConstituent.M3 -> M3
            TideConstituent.L2 -> L2
            TideConstituent._2MK3 -> M2 + O1
            TideConstituent.K2 -> K2
            TideConstituent.M8 -> M2 * 4
            TideConstituent.MS4 -> M2 + S2
            TideConstituent.Z0 -> 0f
        }
    }

}