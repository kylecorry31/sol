package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.geometry.Geometry
import com.kylecorry.sol.math.statistics.Statistics
import com.kylecorry.sol.math.sumOfFloat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

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

    @Test
    fun filterPlane() {
        val random = Random(1)
        val xs = List(200) { listOf(random.nextFloat() * 3f, random.nextFloat() * 3f) }
        val ys = xs.map { 3f * it[0] - 2f * it[1] + 5f }

        val smoothed = LoessFilter(0.1f, 2, minimumSpanSize = 10).filter(xs, ys)

        smoothed.zip(ys).forEach { assertEquals(it.second, it.first, 0.01f) }
    }

    @Test
    fun filterNoisySurface() {
        val random = Random(2)
        val xs = List(600) { listOf(random.nextFloat() * 3f, random.nextFloat() * 3f) }
        val truth = xs.map { 500f + 10f * sin(it[0] * 0.7f) + 8f * cos(it[1] * 0.9f) }
        val ys = truth.map { it + (random.nextFloat() - 0.5f) * 10f }

        val smoothed = LoessFilter(0.1f, 1, minimumSpanSize = 10).filter(xs, ys)

        assertTrue(Statistics.rmse(truth, smoothed) < Statistics.rmse(truth, ys) / 2)
    }

    @Test
    fun robustnessIterationsRejectOutlierInMultipleDimensions() {
        val random = Random(3)
        val xs = List(300) { listOf(random.nextFloat() * 3f, random.nextFloat() * 3f) }
        val truth = xs.map { 2f * it[0] + it[1] }
        val ys = truth.mapIndexed { i, value -> if (i % 25 == 0) value + 100f else value }

        val smoothed = LoessFilter(0.1f, 4, minimumSpanSize = 10).filter(xs, ys)

        assertEquals(truth[1], smoothed[1], 0.5f)
        assertEquals(truth[26], smoothed[26], 0.5f)
    }

    @Test
    fun filterWithEquidistantNeighbors() {
        val xs = (0..14).flatMap { x -> (0..14).map { y -> listOf(x.toFloat(), y.toFloat()) } }
        val ys = xs.map { 10f + 2f * it[0] - it[1] }

        val smoothed = LoessFilter(0.05f, 1, minimumSpanSize = 8) { a, b ->
            Geometry.euclideanDistance(a, b)
        }.filter(xs, ys)

        smoothed.zip(ys).forEach { assertEquals(it.second, it.first, 0.01f) }
    }

    @Test
    fun maximumSpanSizeLimitsTheNeighborhood() {
        // The spike is within the proportional span of point 20, but not within 20 points of it
        val xs = List(200) { listOf(it.toFloat()) }
        val ys = List(200) { if (it == 60) 1000f else it * 2f }

        val unbounded = LoessFilter(0.5f, 0).filter(xs, ys)
        val bounded = LoessFilter(0.5f, 0, maximumSpanSize = 20).filter(xs, ys)

        assertTrue(abs(unbounded[20] - 40f) > 1f)
        assertEquals(40f, bounded[20], 0.01f)
    }

    @Test
    fun spanLargerThanTheDatasetUsesAllOtherPoints() {
        val xs = List(10) { listOf(it.toFloat()) }
        val ys = List(10) { it * 3f }

        val smoothed = LoessFilter(2f, 0).filter(xs, ys)

        smoothed.zip(ys).forEach { assertEquals(it.second, it.first, 0.001f) }
    }

    @Test
    fun distanceFunctionIsCalledWithTheSmoothedPointFirst() {
        val xs = List(20) { listOf(it.toFloat()) }
        val ys = List(20) { it.toFloat() }
        var firstArgumentWasPoint = true

        LoessFilter(0.5f, 0) { p1, p2 ->
            firstArgumentWasPoint = firstArgumentWasPoint && xs.any { it === p1 }
            abs(p1[0] - p2[0])
        }.filter(xs, ys)

        assertTrue(firstArgumentWasPoint)
    }

    @Test
    fun filterPointsInALineUsesTheMean() {
        val random = Random(4)
        val xs = List(100) { listOf(it * 0.1f, it * 0.1f) }
        val ys = List(100) { 5f + (random.nextFloat() - 0.5f) }

        val smoothed = LoessFilter(0.2f, 1).filter(xs, ys)

        assertTrue(smoothed.all { it in 4.5f..5.5f })
    }

    @Test
    fun filterPointsWithTheSameXUsesTheMean() {
        val random = Random(5)
        val xs = List(60) { listOf(2f, random.nextFloat()) }
        val ys = List(60) { 8f + (random.nextFloat() - 0.5f) }

        val smoothed = LoessFilter(0.3f, 1).filter(xs, ys)

        assertTrue(smoothed.all { it in 7.5f..8.5f })
    }

    @Test
    fun filterIsIndependentOfTheScaleOfX() {
        val random = Random(6)
        val xs = List(200) { listOf(random.nextFloat(), random.nextFloat()) }
        val ys = xs.map { 3f * it[0] - 2f * it[1] + 5f }
        val scaledXs = xs.map { point -> point.map { it * 0.001f } }

        val smoothed = LoessFilter(0.1f, 1, minimumSpanSize = 10).filter(scaledXs, ys)

        smoothed.zip(ys).forEach { assertEquals(it.second, it.first, 0.01f) }
    }
}
