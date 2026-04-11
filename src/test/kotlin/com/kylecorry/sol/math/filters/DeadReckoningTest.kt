package com.kylecorry.sol.math.filters

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DeadReckoningTest {

    @Test
    fun deadReckon() {
        val filter = DeadReckoning(1f)

        assertEquals(1f, filter.position, 0.001f)

        assertEquals(3f, filter.calculate(2f, 1f), 0.001f)

        assertEquals(4.5f, filter.calculate(3f, 0.5f), 0.001f)

        assertEquals(3f, filter.calculate(-3f, 0.5f), 0.001f)
    }

}