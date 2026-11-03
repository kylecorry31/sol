package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.science.oceanography.TideConstituent

/**
 * The phase (degrees) of each tidal constituent's equilibrium tide at a moment in time, from the
 * Doodson numbers of the constituent and the mean longitudes of the moon, sun and perigees
 * (Cartwright). Does not include the nodal angle.
 */
internal class AstronomicalArgument private constructor(epochSeconds: Double) {

    private val moon: Double
    private val sun: Double
    private val perigee: Double
    private val solarPerigee = SOLAR_PERIGEE
    private val lunarTime: Double

    init {
        val days = epochSeconds / SECONDS_PER_DAY
        val t = days + UNIX_EPOCH_MJD - MJD_REFERENCE
        moon = 218.3164 + 13.17639648 * t
        sun = 280.4661 + 0.98564736 * t
        perigee = 83.3535 + 0.11140353 * t
        val hours = (epochSeconds - Math.floor(days) * SECONDS_PER_DAY) / 3600.0
        lunarTime = 15.0 * hours - moon + sun
    }

    /**
     * @return the phase in degrees, or null if the constituent has no equilibrium tide
     */
    fun of(constituent: TideConstituent): Double? {
        val c = coefficients[constituent] ?: return null
        val degrees = c[0] * lunarTime + c[1] * moon + c[2] * sun + c[3] * perigee +
                c[4] * solarPerigee + c[5] * 90.0
        return ((degrees % 360) + 360) % 360
    }

    companion object {
        private const val SECONDS_PER_DAY = 86400.0
        private const val UNIX_EPOCH_MJD = 40587.0
        private const val MJD_REFERENCE = 51544.4993
        private const val SOLAR_PERIGEE = 282.8

        fun at(epochSeconds: Double): AstronomicalArgument {
            return AstronomicalArgument(epochSeconds)
        }

        // Multiples of the lunar time, moon, sun, lunar perigee and solar perigee longitudes and of
        // 90 degrees (which converts sine terms into cosine terms)
        private fun c(tau: Int, s: Int, h: Int, p: Int, pp: Int, quarter: Int): DoubleArray {
            return doubleArrayOf(tau.toDouble(), s.toDouble(), h.toDouble(), p.toDouble(), pp.toDouble(), quarter.toDouble())
        }

        private val coefficients = mapOf(
            TideConstituent.M2 to c(2, 0, 0, 0, 0, 0),
            TideConstituent.S2 to c(2, 2, -2, 0, 0, 0),
            TideConstituent.N2 to c(2, -1, 0, 1, 0, 0),
            TideConstituent.K1 to c(1, 1, 0, 0, 0, 1),
            TideConstituent.M4 to c(4, 0, 0, 0, 0, 0),
            TideConstituent.O1 to c(1, -1, 0, 0, 0, -1),
            TideConstituent.M6 to c(6, 0, 0, 0, 0, 0),
            TideConstituent.MK3 to c(3, 1, 0, 0, 0, 1),
            TideConstituent.S4 to c(4, 4, -4, 0, 0, 0),
            TideConstituent.MN4 to c(4, -1, 0, 1, 0, 0),
            TideConstituent.NU2 to c(2, -1, 2, -1, 0, 0),
            TideConstituent.S6 to c(6, 6, -6, 0, 0, 0),
            TideConstituent.MU2 to c(2, -2, 2, 0, 0, 0),
            TideConstituent.TWO_N2 to c(2, -2, 0, 2, 0, 0),
            TideConstituent.OO1 to c(1, 3, 0, 0, 0, 1),
            TideConstituent.LAM2 to c(2, 1, -2, 1, 0, 2),
            TideConstituent.S1 to c(1, 1, -1, 0, 0, 1),
            TideConstituent.M1 to c(1, 0, 0, 0, 0, 1),
            TideConstituent.J1 to c(1, 2, 0, -1, 0, 1),
            TideConstituent.MM to c(0, 1, 0, -1, 0, 0),
            TideConstituent.SSA to c(0, 0, 2, 0, 0, 0),
            TideConstituent.SA to c(0, 0, 1, 0, -1, 0),
            TideConstituent.MSF to c(0, 2, -2, 0, 0, 0),
            TideConstituent.MF to c(0, 2, 0, 0, 0, 0),
            TideConstituent.RHO to c(1, -2, 2, -1, 0, -1),
            TideConstituent.Q1 to c(1, -2, 0, 1, 0, -1),
            TideConstituent.T2 to c(2, 2, -3, 0, 1, 0),
            TideConstituent.R2 to c(2, 2, -1, 0, -1, 2),
            TideConstituent.TWO_Q1 to c(1, -3, 0, 2, 0, -1),
            TideConstituent.P1 to c(1, 1, -2, 0, 0, -1),
            TideConstituent.TWO_SM2 to c(2, 4, -4, 0, 0, 0),
            TideConstituent.M3 to c(3, 0, 0, 0, 0, 0),
            TideConstituent.L2 to c(2, 1, 0, -1, 0, 2),
            TideConstituent.TWO_MK3 to c(3, -1, 0, 0, 0, -1),
            TideConstituent.K2 to c(2, 2, 0, 0, 0, 0),
            TideConstituent.M8 to c(8, 0, 0, 0, 0, 0),
            TideConstituent.MS4 to c(4, 2, -2, 0, 0, 0),
        )
    }
}
