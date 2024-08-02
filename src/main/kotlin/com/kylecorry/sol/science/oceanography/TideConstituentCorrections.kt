package com.kylecorry.sol.science.oceanography

import com.kylecorry.sol.math.SolMath.cube
import com.kylecorry.sol.math.SolMath.power
import com.kylecorry.sol.math.SolMath.square

class ConstituentCorrection(val f: Float, val uv: Float)

class TideConstituentCorrections(
    private val Q1: ConstituentCorrection,
    private val O1: ConstituentCorrection,
    private val P1: ConstituentCorrection,
    private val K1: ConstituentCorrection,
    private val N2: ConstituentCorrection,
    private val M2: ConstituentCorrection,
    private val S2: ConstituentCorrection,
    private val K2: ConstituentCorrection,
    private val L2: ConstituentCorrection,
    private val M1: ConstituentCorrection,
    private val J1: ConstituentCorrection,
    private val MM: ConstituentCorrection,
    private val MF: ConstituentCorrection,
    private val M3: ConstituentCorrection
) {

    fun getUV(constituent: TideConstituent): Float {
        return when (constituent) {
            TideConstituent.M2 -> M2.uv
            TideConstituent.S2 -> S2.uv
            TideConstituent.N2 -> N2.uv
            TideConstituent.K1 -> K1.uv
            TideConstituent.M4 -> M2.uv * 2
            TideConstituent.O1 -> O1.uv
            TideConstituent.M6 -> M2.uv * 3
            TideConstituent.MK3 -> M2.uv + K1.uv
            TideConstituent.S4 -> S2.uv * 2
            TideConstituent.MN4 -> M2.uv + N2.uv
            TideConstituent.NU2 -> M2.uv
            TideConstituent.S6 -> S2.uv * 3
            TideConstituent.MU2 -> M2.uv * 2 - S2.uv
            TideConstituent._2N2 -> M2.uv
            TideConstituent.LAM2 -> M2.uv
            TideConstituent.S1 -> 0f
            TideConstituent.M1 -> M1.uv
            TideConstituent.J1 -> J1.uv
            TideConstituent.MM -> MM.uv
            TideConstituent.SSA -> 0f
            TideConstituent.SA -> 0f
            TideConstituent.MSF -> S2.uv - M2.uv
            TideConstituent.MF -> MF.uv
            TideConstituent.RHO -> M2.uv - K1.uv // NU2 - K1
            TideConstituent.Q1 -> Q1.uv
            TideConstituent.T2 -> 0f
            TideConstituent.R2 -> 0f
            TideConstituent._2Q1 -> N2.uv - J1.uv
            TideConstituent.P1 -> P1.uv
            TideConstituent._2SM2 -> 2 * S2.uv - M2.uv
            TideConstituent.M3 -> M3.uv
            TideConstituent.L2 -> L2.uv
            TideConstituent._2MK3 -> M2.uv + O1.uv
            TideConstituent.K2 -> K2.uv
            TideConstituent.M8 -> M2.uv * 4
            TideConstituent.MS4 -> M2.uv + S2.uv
            TideConstituent.Z0 -> 0f
        }
    }

    fun getF(constituent: TideConstituent): Float {
        return when (constituent) {
            TideConstituent.M2 -> M2.f
            TideConstituent.S2 -> S2.f
            TideConstituent.N2 -> N2.f
            TideConstituent.K1 -> K1.f
            TideConstituent.M4 -> square(M2.f)
            TideConstituent.O1 -> O1.f
            TideConstituent.M6 -> cube(M2.f)
            TideConstituent.MK3 -> M2.f * K1.f
            TideConstituent.S4 -> square(S2.f)
            TideConstituent.MN4 -> M2.f * N2.f
            TideConstituent.NU2 -> M2.f
            TideConstituent.S6 -> cube(S2.f)
            TideConstituent.MU2 -> square(M2.f) * S2.f
            TideConstituent._2N2 -> M2.f
            TideConstituent.LAM2 -> M2.f
            TideConstituent.S1 -> 1f
            TideConstituent.M1 -> M1.f
            TideConstituent.J1 -> J1.f
            TideConstituent.MM -> MM.f
            TideConstituent.SSA -> 1f
            TideConstituent.SA -> 1f
            TideConstituent.MSF -> S2.f * M2.f
            TideConstituent.MF -> MF.f
            TideConstituent.RHO -> M2.f * K1.f // NU2 * K1
            TideConstituent.Q1 -> Q1.f
            TideConstituent.T2 -> 1f
            TideConstituent.R2 -> 1f
            TideConstituent._2Q1 -> N2.f * J1.f
            TideConstituent.P1 -> P1.f
            TideConstituent._2SM2 -> square(S2.f) * M2.f
            TideConstituent.M3 -> M3.f
            TideConstituent.L2 -> L2.f
            TideConstituent._2MK3 -> M2.f * O1.f
            TideConstituent.K2 -> K2.f
            TideConstituent.M8 -> power(M2.f, 4)
            TideConstituent.MS4 -> M2.f * S2.f
            TideConstituent.Z0 -> 1f
        }
    }

}