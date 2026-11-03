package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.science.oceanography.TideConstituent
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

/**
 * The nodal modulation factor (f) and nodal angle (u, degrees) of the tidal constituents, which
 * vary with the 18.6 year cycle of the lunar node and the 8.85 year cycle of the lunar perigee.
 * Follows the OTIS convention used by pyTMD.
 */
internal class NodalCorrection private constructor(nodeDegrees: Double, perigeeDegrees: Double) {

    private val n = Math.toRadians(nodeDegrees)
    private val p = Math.toRadians(perigeeDegrees)

    /**
     * @return the factor f and the angle u in degrees
     */
    fun of(constituent: TideConstituent): Pair<Double, Double> {
        var f = 1.0
        var u = 0.0
        for (term in terms[constituent] ?: emptyList()) {
            val (baseF, baseU) = base(term.base)
            f *= baseF.pow(term.fPower)
            u += term.uMultiple * baseU
        }
        return Pair(f, u)
    }

    private fun base(base: Base): Pair<Double, Double> {
        val sinN = sin(n)
        val cosN = cos(n)
        val sin2N = sin(2 * n)
        val cos2N = cos(2 * n)
        val sin3N = sin(3 * n)
        val sin2P = sin(2 * p)
        val cos2P = cos(2 * p)
        val sin2PN = sin(2 * p - n)
        val cos2PN = cos(2 * p - n)

        return when (base) {
            Base.M2 -> polar(-0.03731 * sinN + 0.00052 * sin2N, 1 - 0.03731 * cosN + 0.00052 * cos2N)
            Base.K1 -> polar(-0.1554 * sinN + 0.0029 * sin2N, 1 + 0.1158 * cosN - 0.0029 * cos2N)
            Base.K2 -> polar(-0.3108 * sinN - 0.0324 * sin2N, 1 + 0.2852 * cosN + 0.0324 * cos2N)
            Base.J1 -> polar(-0.227 * sinN, 1 + 0.169 * cosN)
            Base.OO1 -> polar(-0.640 * sinN - 0.134 * sin2N, 1 + 0.640 * cosN + 0.134 * cos2N)
            Base.M3 -> polar(-0.05644 * sinN, 1 - 0.05644 * cosN)
            Base.L2 -> polar(
                -0.25 * sin2P - 0.11 * sin2PN - 0.04 * sinN,
                1 - 0.25 * cos2P - 0.11 * cos2PN - 0.04 * cosN
            )

            Base.M1 -> polar(
                -0.2294 * sinN - 0.3594 * sin2P - 0.0664 * sin2PN,
                1 + 0.1722 * cosN + 0.3594 * cos2P + 0.0664 * cos2PN
            )

            Base.O1 -> Pair(
                hypot(
                    0.189 * sinN - 0.0058 * sin2N,
                    1 + 0.189 * cosN - 0.0058 * cos2N
                ),
                10.8 * sinN - 1.3 * sin2N + 0.2 * sin3N
            )

            Base.Q1 -> Pair(
                hypot(1 + 0.188 * cosN, 0.188 * sinN),
                Math.toDegrees(atan(0.189 * sinN / (1 + 0.189 * cosN)))
            )

            Base.MM -> Pair(1 - 0.130 * cosN, 0.0)
            Base.MF -> Pair(
                1.043 + 0.414 * cosN,
                -23.7 * sinN + 2.7 * sin2N - 0.4 * sin3N
            )
        }
    }

    private fun polar(sinPart: Double, cosPart: Double): Pair<Double, Double> {
        return Pair(hypot(sinPart, cosPart), Math.toDegrees(atan2(sinPart, cosPart)))
    }

    private enum class Base { M2, K1, K2, O1, Q1, OO1, J1, M1, MM, MF, L2, M3 }

    // The constituent's f is the product of each base's f raised to fPower, and its u is the sum of
    // each base's u scaled by uMultiple. Constituents that are not listed are unmodulated.
    private class Term(val base: Base, val fPower: Double, val uMultiple: Double)

    companion object {
        // Mean longitudes (Cartwright) at the Unix epoch (MJD 40587)
        private const val EPOCH_NODE = 125.0445 - 0.05295377 * (40587 - 51544.4993)
        private const val EPOCH_PERIGEE = 83.3535 + 0.11140353 * (40587 - 51544.4993)
        private const val NODE_RATE_PER_DAY = -0.05295377
        private const val PERIGEE_RATE_PER_DAY = 0.11140353
        private const val SECONDS_PER_DAY = 86400.0

        fun at(epochSeconds: Long): NodalCorrection {
            val days = epochSeconds / SECONDS_PER_DAY
            return NodalCorrection(
                normalize(EPOCH_NODE + NODE_RATE_PER_DAY * days),
                normalize(EPOCH_PERIGEE + PERIGEE_RATE_PER_DAY * days)
            )
        }

        private fun normalize(degrees: Double): Double {
            return ((degrees % 360) + 360) % 360
        }

        private fun of(base: Base, power: Int = 1, uMultiple: Int = power): Term {
            return Term(base, power.toDouble(), uMultiple.toDouble())
        }

        private fun m2(power: Int) = listOf(of(Base.M2, power))

        private val terms = mapOf(
            TideConstituent.M2 to m2(1),
            TideConstituent.N2 to m2(1),
            TideConstituent.NU2 to m2(1),
            TideConstituent.MU2 to m2(1),
            TideConstituent.TWO_N2 to m2(1),
            TideConstituent.MS4 to m2(1),
            TideConstituent.TWO_SM2 to listOf(of(Base.M2, 1, -1)),
            TideConstituent.M4 to m2(2),
            TideConstituent.MN4 to m2(2),
            TideConstituent.M6 to m2(3),
            TideConstituent.M8 to m2(4),
            TideConstituent.K1 to listOf(of(Base.K1)),
            TideConstituent.MK3 to listOf(of(Base.M2), of(Base.K1)),
            TideConstituent.TWO_MK3 to listOf(of(Base.M2, 2), of(Base.K1, 1, -1)),
            TideConstituent.RHO to listOf(of(Base.Q1)),
            TideConstituent.O1 to listOf(of(Base.O1)),
            TideConstituent.Q1 to listOf(of(Base.Q1)),
            TideConstituent.TWO_Q1 to listOf(of(Base.Q1)),
            TideConstituent.OO1 to listOf(of(Base.OO1)),
            TideConstituent.J1 to listOf(of(Base.J1)),
            TideConstituent.M1 to listOf(of(Base.M1)),
            TideConstituent.MM to listOf(of(Base.MM)),
            TideConstituent.MF to listOf(of(Base.MF)),
            TideConstituent.K2 to listOf(of(Base.K2)),
            TideConstituent.L2 to listOf(of(Base.L2)),
            TideConstituent.M3 to listOf(of(Base.M3)),
        )
    }
}
