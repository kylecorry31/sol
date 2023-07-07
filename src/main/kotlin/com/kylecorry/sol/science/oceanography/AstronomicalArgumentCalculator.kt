package com.kylecorry.sol.science.oceanography

internal object AstronomicalArgumentCalculator {

    // TODO: Calculate like pytide https://github.com/sam-cox/pytides/blob/master/pytides/constituent.py

    fun get(constituent: TideConstituent, year: Int): Float {
        // From https://www.dfo-mpo.gc.ca/science/data-donnees/tidal-marees/argument-u-v-eng.html
        return if (year <= 2021) {
            get2021(constituent)
        } else {
            get2022(constituent)
        }
    }

    private fun get2021(constituent: TideConstituent): Float {
        return when (constituent) {
            TideConstituent.M2 -> 304.098f
            TideConstituent.S2 -> 0.138f
            TideConstituent.N2 -> 33.743f
            TideConstituent.K1 -> 2.518f
            TideConstituent.M4 -> 248.196f
            TideConstituent.O1 -> 304.678f
            TideConstituent.P1 -> 348.423f
            TideConstituent.L2 -> 17.969f // Calculated
            TideConstituent.K2 -> 184.784f
            TideConstituent.MS4 -> 304.236f
            TideConstituent.Z0 -> 0f
        }
    }

    private fun get2022(constituent: TideConstituent): Float {
        return when (constituent) {
            TideConstituent.M2 -> 45.013f
            TideConstituent.S2 -> 0.113f
            TideConstituent.N2 -> 46.36f
            TideConstituent.K1 -> 3.577f
            TideConstituent.M4 -> 90.026f // M2 * 2
            TideConstituent.O1 -> 44.031f
            TideConstituent.P1 -> 348.805f
            TideConstituent.L2 -> 213.77879f // Calculated
            TideConstituent.K2 -> 186.65f
            TideConstituent.MS4 -> 45.126f // M2 + S2
            TideConstituent.Z0 -> 0f
        }
    }

}