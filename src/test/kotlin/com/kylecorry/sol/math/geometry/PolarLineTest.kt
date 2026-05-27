package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.sqrt

internal class PolarLineTest {

    @Test
    fun evaluateVerticalLine() {
        val line = PolarLine(rho = 2f, thetaRadians = 0f)

        assertEquals(0f, line.evaluate(Vector2(2f, -5f)), 0.0001f)
        assertEquals(3f, line.evaluate(Vector2(5f, 1f)), 0.0001f)
        assertEquals(-4f, line.evaluate(Vector2(-2f, 3f)), 0.0001f)
    }

    @Test
    fun evaluateHorizontalLine() {
        val line = PolarLine(rho = 3f, thetaRadians = PI.toFloat() / 2f)

        assertEquals(0f, line.evaluate(Vector2(-4f, 3f)), 0.0001f)
        assertEquals(2f, line.evaluate(Vector2(1f, 5f)), 0.0001f)
        assertEquals(-4f, line.evaluate(Vector2(2f, -1f)), 0.0001f)
    }

    @Test
    fun evaluateAngledLine() {
        val line = PolarLine(rho = sqrt(2f), thetaRadians = PI.toFloat() / 4f)

        assertEquals(0f, line.evaluate(Vector2(1f, 1f)), 0.0001f)
        assertEquals(sqrt(2f), line.evaluate(Vector2(2f, 2f)), 0.0001f)
        assertEquals(-sqrt(2f), line.evaluate(Vector2(0f, 0f)), 0.0001f)
    }
}
