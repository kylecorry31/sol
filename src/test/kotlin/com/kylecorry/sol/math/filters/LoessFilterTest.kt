package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.sumOfFloat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.pow

class LoessFilterTest {
    @Test
    fun filter() {
        val values = (0..100).map { it.toFloat() to it.toFloat() }

        val filter = LoessFilter(0.3f, 4)

        val actual = filter.filter(values.map { listOf(it.first) }, values.map { it.second })

        val fitResiduals = actual.zip(values).sumOfFloat {
            (it.second.second - it.first).pow(2)
        }

        assertEquals(0.0f, fitResiduals, 0.0001f)
    }

    @Test
    fun filterEmpty() {
        val values = emptyList<Pair<Float, Float>>()

        val filter = LoessFilter(0.3f, 4)

        val actual = filter.filter(values.map { listOf(it.first) }, values.map { it.second })

        assertTrue(actual.isEmpty())
    }

    @Test
    fun filterLessThan3() {
        val values = listOf(1f to 1f, 2f to 2f)

        val filter = LoessFilter(0.3f, 4)

        val actual = filter.filter(values.map { listOf(it.first) }, values.map { it.second })

        assertEquals(values.map { it.second }, actual)
    }

    @Test
    fun filterMultipleWithSameX() {
        val values = listOf(1f to 1f, 2f to 2f, 1f to 3f)

        val filter = LoessFilter(0.3f, 4)

        val actual = filter.filter(values.map { listOf(it.first) }, values.map { it.second })

        assertEquals(values.map { it.second }, actual)
    }

    @Test
    fun customDistanceFunction() {
        val values = (0..100).map { it.toFloat() to it.toFloat() }

        val filter = LoessFilter(0.3f, 4) { p1, p2 ->
            p1.zip(p2).sumOfFloat { (it.first - it.second).pow(2) }
        }

        val actual = filter.filter(values.map { listOf(it.first) }, values.map { it.second })

        val fitResiduals = actual.zip(values).sumOfFloat {
            (it.second.second - it.first).pow(2)
        }

        assertEquals(0.0f, fitResiduals, 0.0001f)
    }

    @Test
    fun robustnessIterationsRejectOutlier() {
        val xs = (0..20).map { listOf(it.toFloat()) }
        val ys = (0..20).map { if (it == 10) 100f else it.toFloat() }

        val smoothed = LoessFilter(span = 0.4f, robustnessIterations = 20).filter(xs, ys)

        assertEquals(9f, smoothed[9], 1f)
    }

    @Test
    fun filterConsecutiveNoisyReadings() {
        val xs = List(30) { listOf(it.toFloat()) }
        val ys = List(30) { index ->
            if (index in 10..19) {
                if (index % 2 == 0) 900f else 1100f
            } else {
                1000f + (index % 3 - 1) * 0.1f
            }
        }

        val initial = LoessFilter(
            span = 0.15f,
            robustnessIterations = 0,
            minimumSpanSize = 10
        ).filter(xs, ys)
        val smoothed = LoessFilter(
            span = 0.15f,
            robustnessIterations = 1,
            minimumSpanSize = 10
        ).filter(xs, ys)

        assertEquals(ys.size, smoothed.size)
        assertTrue(smoothed.all { it.isFinite() })
        assertEquals(initial[13], smoothed[13])
        assertEquals(initial[16], smoothed[16])
    }
}
