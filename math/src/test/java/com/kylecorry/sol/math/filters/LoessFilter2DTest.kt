package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.statistics.Statistics
import com.kylecorry.sol.math.sumOfFloat
import com.kylecorry.sol.math.toVector2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

internal class LoessFilter2DTest {

    @Test
    fun filterSin() {
        val random = Random(1)
        val indices = (0..100).map { it / 100f }
        val values = indices.map { it to sin(it) + (random.nextFloat() - 0.5f) * 0.1f }
            .map { it.toVector2() }
        val expected = indices.map { it to sin(it) }.map { it.toVector2() }

        val filter = LoessFilter2D(0.3f, 4)

        val actual = filter.filter(values)

        val fitResiduals = Statistics.rmse(expected.map { it.y }, actual.map { it.y })
        val originalResiduals = Statistics.rmse(expected.map { it.y }, values.map { it.y })

        assertTrue(fitResiduals < originalResiduals)
        assertEquals(0.006f, fitResiduals, 0.001f)
    }

    @Test
    fun filterLine() {
        val values = (0..100).map { it.toFloat() to it.toFloat() }.map { it.toVector2() }

        val filter = LoessFilter2D(0.3f, 4)

        val actual = filter.filter(values)

        val fitResiduals = actual.zip(values).sumOfFloat {
            (it.second.y - it.first.y).pow(2)
        }

        assertEquals(0.0f, fitResiduals, 0.00001f)
    }

    @Test
    fun filterSamePoint() {
        val values = listOf(
            Vector2(x = 1f, y = 1.0f),
            Vector2(x = 1f, y = 1.0f),
            Vector2(x = 1f, y = 1.0f)
        )

        val filter = LoessFilter2D(0.25f, 2, minimumSpanSize = 10)

        val actual = filter.filter(values)

        val fitResiduals = actual.zip(values).sumOfFloat {
            (it.second.y - it.first.y).pow(2)
        }

        assertEquals(listOf(Vector2(1f, 1f), Vector2(1f, 1f), Vector2(1f, 1f)), values)
        assertEquals(0.0f, fitResiduals, 0.00001f)
    }

    @Test
    fun filterPoints() {
        val values = (0..1).map { it.toFloat() to it.toFloat() }.map { it.toVector2() }

        val filter = LoessFilter2D(0.3f, 4)

        val actual = filter.filter(values)

        val fitResiduals = actual.zip(values).sumOfFloat {
            (it.second.y - it.first.y).pow(2)
        }

        assertEquals(0.0f, fitResiduals, 0.00001f)
    }

    @Test
    fun filterPoint() {
        val values = listOf(Vector2(1f, 2f))

        val filter = LoessFilter2D(0.3f, 4)

        val actual = filter.filter(values)

        val fitResiduals = actual.zip(values).sumOfFloat {
            (it.second.y - it.first.y).pow(2)
        }

        assertEquals(0.0f, fitResiduals, 0.00001f)
    }
}