package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.sumOfFloat
import com.kylecorry.sol.math.toVector2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

internal class LoessFilterTest {

    @Test
    fun filterSin() {
        val random = Random(1)
        val values = (0..100).map { it / 100f to sin(it / 100f) + (random.nextFloat() - 0.5f) * 0.1f }.map { it.toVector2() }
        val expected = (0..100).map { it / 100f to sin(it / 100f) }.map { it.toVector2() }

        val filter = LoessFilter(0.3f, 4)

        val actual = filter.filter(values)

        val fitResiduals = expected.zip(actual).sumOfFloat {
            (it.second.y - it.first.y).pow(2)
        }

        val originalResiduals = expected.zip(values).sumOfFloat {
            (it.second.y - it.first.y).pow(2)
        }

        assertTrue(fitResiduals < originalResiduals)
        assertEquals(0.003f, fitResiduals, 0.001f)
    }

    @Test
    fun filterLine() {
        val values = (0..100).map { it.toFloat() to it.toFloat() }.map { it.toVector2() }

        val filter = LoessFilter(0.3f, 4)

        val actual = filter.filter(values)

        val fitResiduals = actual.zip(values).sumOfFloat {
            (it.second.y - it.first.y).pow(2)
        }

        assertEquals(0.0f, fitResiduals, 0.00001f)
    }

    @Test
    fun filterPoints() {
        val values = (0..1).map { it.toFloat() to it.toFloat() }.map { it.toVector2() }

        val filter = LoessFilter(0.3f, 4)

        val actual = filter.filter(values)

        val fitResiduals = actual.zip(values).sumOfFloat {
            (it.second.y - it.first.y).pow(2)
        }

        assertEquals(0.0f, fitResiduals, 0.00001f)
    }

    @Test
    fun filterPoint() {
        val values = listOf(Vector2(1f, 2f))

        val filter = LoessFilter(0.3f, 4)

        val actual = filter.filter(values)

        val fitResiduals = actual.zip(values).sumOfFloat {
            (it.second.y - it.first.y).pow(2)
        }

        assertEquals(0.0f, fitResiduals, 0.00001f)
    }
}