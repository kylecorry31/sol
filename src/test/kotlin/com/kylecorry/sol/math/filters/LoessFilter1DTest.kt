package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.sumOfFloat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.pow

class LoessFilter1DTest {

    @Test
    fun filter() {
        val values = (0..100).map { it.toFloat() }

        val filter = LoessFilter1D(0.3f, 4)

        val actual = filter.filter(values)

        val fitResiduals = actual.zip(values).sumOfFloat {
            (it.second - it.first).pow(2)
        }

        assertEquals(0.0f, fitResiduals, 0.0001f)
    }
}