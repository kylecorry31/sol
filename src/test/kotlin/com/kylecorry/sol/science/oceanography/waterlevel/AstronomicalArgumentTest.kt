package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.science.oceanography.TideConstituent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

internal class AstronomicalArgumentTest {

    // Expected values are from pyTMD at 2030-03-15 06:00 UTC
    @Test
    fun matchesPyTMD() {
        val time = LocalDateTime.of(2030, 3, 15, 6, 0).toEpochSecond(ZoneOffset.UTC)
        val argument = AstronomicalArgument.at(time.toDouble())

        val expected = mapOf(
            TideConstituent.M2 to 278.071,
            TideConstituent.K1 to 172.896,
            TideConstituent.O1 to 105.174,
            TideConstituent.TWO_N2 to 134.785,
            TideConstituent.S1 to 180.0,
            TideConstituent.SA to 70.096,
            TideConstituent.MF to 247.722,
            TideConstituent.T2 to 109.904,
            TideConstituent.L2 to 349.714,
            TideConstituent.TWO_MK3 to 23.245,
        )
        for ((constituent, degrees) in expected) {
            assertEquals(degrees, argument.of(constituent)!!, 0.05, constituent.name)
        }
    }

    @Test
    fun noEquilibriumTideForMeanLevel() {
        assertNull(AstronomicalArgument.at(0.0).of(TideConstituent.Z0))
    }
}
