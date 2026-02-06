package com.kylecorry.sol.science.oceanography.waterlevel
import com.kylecorry.sol.math.analysis.Trigonometry

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.science.oceanography.TidalHarmonic
import com.kylecorry.sol.science.oceanography.TideConstituent
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class HarmonicWaterLevelCalculator(private val harmonics: List<TidalHarmonic>) :
    IWaterLevelCalculator {

    override fun calculate(time: ZonedDateTime): Float {
        val corrections = corrections2025
        val t = Duration.between(startDate, time).seconds / 3600f
        val heights = harmonics.map {
            val f = corrections[it.constituent]?.first ?: 0.0
            val uv = corrections[it.constituent]?.second ?: 0.0

            (f * it.amplitude * Trigonometry.cosDegrees(it.constituent.speed * t + uv - it.phase)).toFloat()
        }
        return heights.sum()
    }

    companion object {

        private val startDate = ZonedDateTime.of(
            LocalDateTime.of(2025, 1, 1, 0, 0),
            ZoneId.of("UTC")
        )

        private val corrections2025 = mutableMapOf(
            TideConstituent.M2 to Pair(0.963222492260565, 324.57478670830164),
            TideConstituent.S2 to Pair(1.0, 0.0),
            TideConstituent.N2 to Pair(0.963222492260565, 46.61961828829067),
            TideConstituent.K1 to Pair(1.1128712937492757, 10.704425249700023),
            TideConstituent.M4 to Pair(0.9277975695966542, 289.1495734166033),
            TideConstituent.O1 to Pair(1.1831524803387703, 313.955419278193),
            TideConstituent.M6 to Pair(0.8936754873001843, 253.72436012490496),
            TideConstituent.MK3 to Pair(1.0719426611304168, 335.27921195800167),
            TideConstituent.S4 to Pair(1.0, 0.0),
            TideConstituent.MN4 to Pair(0.9277975695966542, 11.194404996592311),
            TideConstituent.NU2 to Pair(0.963222492260565, 207.16110951964515),
            TideConstituent.S6 to Pair(1.0, 0.0),
            TideConstituent.MU2 to Pair(0.963222492260565, 289.2059410996342),
            TideConstituent._2N2 to Pair(0.963222492260565, 128.6644498682797),
            TideConstituent.OO1 to Pair(1.773757210396645, 247.319577286676),
            TideConstituent.S1 to Pair(1.0, 90.0),
            TideConstituent.M1 to Pair(1.5203581947477462, 61.49071299598883),
            TideConstituent.J1 to Pair(1.1689573556187667, 288.57013361419786),
            TideConstituent.MM to Pair(0.8700443764000282, 277.955168420011),
            TideConstituent.SSA to Pair(1.0, 201.81131558629568),
            TideConstituent.SA to Pair(1.0, 358.1056577931478),
            TideConstituent.MSF to Pair(1.0, 35.36884560866747),
            TideConstituent.MF to Pair(1.4568586782337563, 236.67067358053737),
            TideConstituent.Q1 to Pair(1.1879459793260136, 36.00828926341387),
            TideConstituent.T2 to Pair(1.0, 1.8943422068521727),
            TideConstituent.R2 to Pair(1.0, 178.10565779314783),
            TideConstituent._2Q1 to Pair(1.1879459793260136, 118.0531208434029),
            TideConstituent.P1 to Pair(1.0, 349.09434220685216),
            TideConstituent._2SM2 to Pair(0.963222492260565, 35.42521329169835),
            TideConstituent.M3 to Pair(0.9435804183902405, 306.85719239217514),
            TideConstituent.L2 to Pair(0.7270443001480632, 43.6183351854761),
            TideConstituent._2MK3 to Pair(1.0325192816144622, 278.44514816690327),
            TideConstituent.K2 to Pair(1.317494954643962, 201.38457889735355),
            TideConstituent.M8 to Pair(0.8608083301494583, 218.2991468332066),
            TideConstituent.MS4 to Pair(0.963222492260565, 324.57478670830164),
            TideConstituent.RHO to Pair(1.0719426611304168, 313.8703614586016),
            TideConstituent.LAM2 to Pair(0.963222492260565, 324.57478670830164),
        )
    }

}