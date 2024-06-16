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
}