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
        assertEquals(0.007f, fitResiduals, 0.001f)
    }

    @Test
    fun filterLine() {
        val values = (0..100).map { it.toFloat() to it.toFloat() }.map { it.toVector2() }

        val filter = LoessFilter2D(0.3f, 4)

        val actual = filter.filter(values)

        val fitResiduals = actual.zip(values).sumOfFloat {
            (it.second.y - it.first.y).pow(2)
        }

        assertEquals(0.0f, fitResiduals, 0.0001f)
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

        assertEquals(listOf(Vector2(1f, 1f), Vector2(1f, 1f), Vector2(1f, 1f)), actual)
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

    @Test
    fun filterLineReversed() {
        val values = (0..100).map { it.toFloat() to it.toFloat() }.reversed().map { it.toVector2() }

        val filter = LoessFilter2D(0.3f, 4)

        val actual = filter.filter(values)

        val fitResiduals = actual.zip(values).sumOfFloat {
            (it.second.y - it.first.y).pow(2)
        }

        assertEquals(0.0f, fitResiduals, 0.0001f)
    }

    @Test
    fun robustnessIterationsRejectOutlier() {
        val values = (0..100).map {
            Vector2(it.toFloat(), if (it == 50) 100f else it.toFloat())
        }

        val smoothed = LoessFilter2D(span = 0.4f, robustnessIterations = 4).filter(values)

        assertEquals(49f, smoothed[49].y, 3f)
    }

    @Test
    fun filterConsecutiveNoisyReadings() {
        val values = List(30) { index ->
            val value = if (index in 10..19) {
                if (index % 2 == 0) 900f else 1100f
            } else {
                1000f + (index % 3 - 1) * 0.1f
            }
            Vector2(index.toFloat(), value)
        }

        val initial = LoessFilter2D(
            span = 0.15f,
            robustnessIterations = 0,
            minimumSpanSize = 10
        ).filter(values)
        val smoothed = LoessFilter2D(
            span = 0.15f,
            robustnessIterations = 1,
            minimumSpanSize = 10
        ).filter(values)

        assertEquals(values.size, smoothed.size)
        assertTrue(smoothed.all { it.x.isFinite() && it.y.isFinite() })
        assertEquals(initial[14].y, smoothed[14].y)
        assertEquals(initial[15].y, smoothed[15].y)
    }
}
