package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.science.oceanography.TideConstituent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

internal class NodalCorrectionTest {

    // Expected values are from pyTMD at 00:00 UTC
    @Test
    fun matchesPyTMD() {
        val expected = mapOf(
            2020 to mapOf(
                TideConstituent.M2 to Pair(1.003, -2.13),
                TideConstituent.K1 to Pair(1.0062, -8.884),
                TideConstituent.O1 to Pair(1.0094, 10.77),
                TideConstituent.Q1 to Pair(1.0035, 10.824),
                TideConstituent.K2 to Pair(0.9944, -17.864),
                TideConstituent.L2 to Pair(0.7455, 1.773),
                TideConstituent.M1 to Pair(1.3669, -9.662),
                TideConstituent.MK3 to Pair(1.0092, -11.014),
                TideConstituent.TWO_MK3 to Pair(1.0122, 4.624),
                TideConstituent.M4 to Pair(1.006, -4.26),
                TideConstituent.MF to Pair(1.0118, -23.649),
                TideConstituent.OO1 to Pair(1.0262, -37.03),
            ),
            2030 to mapOf(
                TideConstituent.M2 to Pair(1.0061, 2.108),
                TideConstituent.K1 to Pair(0.9965, 8.911),
                TideConstituent.O1 to Pair(0.9938, -10.892),
                TideConstituent.Q1 to Pair(0.988, -10.889),
                TideConstituent.K2 to Pair(0.9708, 17.805),
                TideConstituent.L2 to Pair(1.1801, -7.739),
                TideConstituent.M1 to Pair(0.8282, -6.6),
                TideConstituent.MK3 to Pair(1.0026, 11.019),
                TideConstituent.TWO_MK3 to Pair(1.0087, -4.695),
                TideConstituent.M4 to Pair(1.0122, 4.216),
                TideConstituent.MF to Pair(0.9778, 23.888),
                TideConstituent.OO1 to Pair(0.9717, 37.41),
            )
        )

        for ((year, constituents) in expected) {
            val time = LocalDateTime.of(year, 3, 15, 0, 0).toEpochSecond(ZoneOffset.UTC)
            val nodal = NodalCorrection.at(time)
            for ((constituent, value) in constituents) {
                val (f, u) = nodal.of(constituent)
                assertEquals(value.first, f, 0.003, "$year $constituent f")
                assertEquals(value.second, u, 0.5, "$year $constituent u")
            }
        }
    }

    @Test
    fun unmodulatedConstituents() {
        val nodal = NodalCorrection.at(0)
        val (f, u) = nodal.of(TideConstituent.S2)
        assertEquals(1.0, f, 0.0)
        assertEquals(0.0, u, 0.0)
    }
}
