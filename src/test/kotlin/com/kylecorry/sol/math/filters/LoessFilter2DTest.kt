package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.statistics.Statistics
import com.kylecorry.sol.math.sumOfFloat
import com.kylecorry.sol.math.toVector2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

internal class LoessFilter2DTest {

    @Test
    fun getSpanForNoiseUsesMinimumSpanWhenAlreadySmooth() {
        val xs = List(500) { it.toFloat() }

        val span = LoessFilter2D.getSpanForNoise(xs, 0.1f, 1f, 10f, 100f, 10f)

        assertEquals(10f, span)
    }

    @Test
    fun getSpanForNoiseUsesMaximumSpanWhenTargetIsUnreachable() {
        val xs = List(500) { it.toFloat() }

        // The step doesn't evenly divide the range, so the last step is clamped
        val span = LoessFilter2D.getSpanForNoise(xs, 5f, 0.001f, 10f, 105f, 10f)

        assertEquals(105f, span)
    }

    @Test
    fun getSpanForNoiseWithNoPoints() {
        assertEquals(10f, LoessFilter2D.getSpanForNoise(emptyList(), 5f, 1f, 10f, 100f, 10f))
    }

    @Test
    fun getSpanForNoiseWidensForLowerTargets() {
        val xs = List(2000) { it.toFloat() }

        val loose = LoessFilter2D.getSpanForNoise(xs, 1f, 0.2f, 5f, 500f, 5f)
        val strict = LoessFilter2D.getSpanForNoise(xs, 1f, 0.1f, 5f, 500f, 5f)

        // For evenly spaced points with a spacing of 1, the noise is 0.19 at a span of 20 and 0.10 at a span of 70
        assertEquals(20f, loose, 5f)
        assertEquals(70f, strict, 10f)
        assertTrue(strict > loose)
    }

    @Test
    fun getSpanForNoiseWidensForSparserPoints() {
        val dense = List(2000) { it.toFloat() }
        val sparse = List(1000) { it * 2f }

        val denseSpan = LoessFilter2D.getSpanForNoise(dense, 1f, 0.15f, 5f, 500f, 5f)
        val sparseSpan = LoessFilter2D.getSpanForNoise(sparse, 1f, 0.15f, 5f, 500f, 5f)

        // Fewer points within a distance average away less of the noise
        assertTrue(sparseSpan > denseSpan)
    }

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
    fun filterDoesNotExtrapolateTheLastPoint() {
        val values = listOf(
            Vector2(0f, 30f),
            Vector2(0.6f, 14.9f),
            Vector2(1.2f, 49.8f),
            Vector2(1.6f, 11.1f)
        )

        val smoothed = LoessFilter2D(0.1f, 1, minimumSpanSize = 10).filter(values)

        assertTrue(smoothed.all { it.y in 11.1f..49.8f })
    }

    @Test
    fun filterLineKeepsTheLastPoints() {
        val values = (0..29).map { Vector2(it.toFloat(), 3f * it) }

        val smoothed = LoessFilter2D(0.3f, 2).filter(values)

        smoothed.takeLast(3).zip(values.takeLast(3)).forEach {
            assertEquals(it.second.y, it.first.y, 0.001f)
        }
    }

    @Test
    fun lastPointIsInfluencedByItsNeighbors() {
        val spanSize = 10
        val values = List(30) { Vector2(it.toFloat(), if (it == 29) 100f else 0f) }

        val smoothed = LoessFilter2D(0.1f, 0, minimumSpanSize = spanSize).filter(values)

        val window = values.takeLast(spanSize).map { it.y }
        assertTrue(smoothed.last().y in window.min()..window.max())
        assertTrue(smoothed.last().y > 0f)
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
    fun robustnessIterationsFullyRejectOutlierOnExactData() {
        val values = (0..100).map {
            Vector2(it.toFloat(), if (it == 50) 200f else it * 2f)
        }

        val smoothed = LoessFilter2D(span = 0.4f, robustnessIterations = 4).filter(values)

        assertEquals(98f, smoothed[49].y, 0.01f)
        assertEquals(102f, smoothed[51].y, 0.01f)
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

    @Test
    fun filterConstantSignal() {
        val values = List(200) { Vector2(it * 0.5f, 42f) }

        val smoothed = LoessFilter2D(0.2f, 2).filter(values)

        assertTrue(smoothed.all { abs(it.y - 42f) < 1e-4f })
    }

    @Test
    fun filterLineWithSpanLimits() {
        val values = List(300) { Vector2(it * 0.25f, 3f * it * 0.25f - 7f) }
        val filters = listOf(
            LoessFilter2D(0.1f, 1, minimumSpanSize = 10),
            LoessFilter2D(0.3f, 3, maximumSpanSize = 20),
            LoessFilter2D(0.01f, 2, minimumSpanSize = 30),
            LoessFilter2D(0.5f, 2, maximumSpanDistance = 10f)
        )

        for (filter in filters) {
            val smoothed = filter.filter(values)
            assertEquals(values.map { it.x }, smoothed.map { it.x })
            smoothed.zip(values).forEach { assertEquals(it.second.y, it.first.y, 0.01f) }
        }
    }

    @Test
    fun filterNoisySignalWithOutliers() {
        val truth = (0..999).map { Vector2(it * 1.5f, profile(it)) }
        val random = Random(3)
        val values = truth.mapIndexed { i, point ->
            val outlier = if (i % 97 == 96) 40f else 0f
            Vector2(point.x, point.y + (random.nextFloat() - 0.5f) * 4f + outlier)
        }

        val smoothed = LoessFilter2D(0.03f, 2, minimumSpanSize = 10).filter(values)

        val smoothedError = Statistics.rmse(truth.map { it.y }, smoothed.map { it.y })
        val rawError = Statistics.rmse(truth.map { it.y }, values.map { it.y })
        assertTrue(smoothedError < rawError / 2)
        assertEquals(truth[96].y, smoothed[96].y, 2f)
    }

    @Test
    fun filterIrregularlySpacedX() {
        val random = Random(8)
        var x = 0f
        val truth = List(500) {
            x += random.nextFloat() * 3f + 0.01f
            Vector2(x, sin(x * 0.05f) * 20f)
        }
        val values = truth.map { Vector2(it.x, it.y + (random.nextFloat() - 0.5f) * 4f) }

        val smoothed = LoessFilter2D(0.03f, 2).filter(values)

        val smoothedError = Statistics.rmse(truth.map { it.y }, smoothed.map { it.y })
        val rawError = Statistics.rmse(truth.map { it.y }, values.map { it.y })
        assertTrue(smoothedError < rawError / 2)
    }

    @Test
    fun filterDuplicateX() {
        val random = Random(5)
        val values = List(300) { Vector2((it / 3).toFloat(), sin(it / 30f) + random.nextFloat() * 0.1f) }

        val smoothed = LoessFilter2D(0.1f, 2).filter(values)

        assertEquals(values.map { it.x }, smoothed.map { it.x })
        assertTrue(smoothed.all { it.y.isFinite() && it.y in -1.2f..1.2f })
    }

    @Test
    fun filterUnsortedInputMatchesSortedInput() {
        val sorted = List(400) { Vector2(it * 1.5f, profile(it) + (it % 7) * 0.3f) }
        val unsorted = sorted.shuffled(Random(2))
        val filter = LoessFilter2D(0.1f, 2, minimumSpanSize = 5)

        val expected = filter.filter(sorted).associate { it.x to it.y }
        val actual = filter.filter(unsorted)

        assertEquals(unsorted.map { it.x }, actual.map { it.x })
        actual.forEach { assertEquals(expected[it.x]!!, it.y, 1e-3f) }
    }

    @Test
    fun filterSmallInputs() {
        for (size in 0..8) {
            val random = Random(size)
            val values = List(size) { Vector2(it.toFloat(), random.nextFloat() * 10f) }

            val smoothed = LoessFilter2D(0.5f, 2, minimumSpanSize = 3).filter(values)

            assertEquals(values.map { it.x }, smoothed.map { it.x })
            assertTrue(smoothed.all { it.y.isFinite() })
        }
    }

    @Test
    fun maximumSpanSizeLimitsTheNeighborhood() {
        // The spike is within the proportional span of point 20, but not within 20 points of it
        val values = List(200) { Vector2(it.toFloat(), if (it == 60) 1000f else it * 2f) }

        val unbounded = LoessFilter2D(0.5f, 0).filter(values)
        val bounded = LoessFilter2D(0.5f, 0, maximumSpanSize = 20).filter(values)

        assertTrue(abs(unbounded[20].y - 40f) > 1f)
        assertEquals(40f, bounded[20].y, 0.01f)
    }

    @Test
    fun filterDoesNotExtrapolateWhenTheNeighborsAreNearlyRejected() {
        // The outliers at 1, 4, and 11 nearly zero the robustness weight of the neighbors of point 9, which
        // used to leave a span that was too bunched up to fit a line and put point 9 at about -3
        val xs = listOf(
            0f, 0.05290934f, 0.10578137f, 0.13085011f, 0.1661629f, 0.1965353f,
            0.23292394f, 0.26614529f, 0.31784117f, 0.35475302f, 0.38622153f, 0.42073753f
        )
        val ys = listOf(
            100.2711f, 79.0544f, 101.62445f, 102.62003f, 91.88113f, 103.67919f,
            103.81322f, 106.1824f, 105.86272f, 106.64763f, 108.38954f, 69.55956f
        )
        val values = xs.zip(ys).map { Vector2(it.first, it.second) }

        val filtered = LoessFilter2D(0.3f, 1, minimumSpanSize = 10, maximumSpanDistance = 0.04f).filter(values)

        assertEquals(ys[9], filtered[9].y, 5f)
        filtered.forEach {
            assertTrue(it.y in ys.min() - 10f..ys.max() + 10f, "Smoothed value was outside of the data: ${it.y}")
        }
    }

    @Test
    fun minimumSpanSizeWidensTheNeighborhood() {
        val random = Random(4)
        val truth = List(600) { Vector2(it.toFloat(), profile(it)) }
        val values = truth.map { Vector2(it.x, it.y + (random.nextFloat() - 0.5f) * 6f) }

        val narrow = LoessFilter2D(0.01f, 0).filter(values)
        val widened = LoessFilter2D(0.01f, 0, minimumSpanSize = 30).filter(values)

        val narrowError = Statistics.rmse(truth.map { it.y }, narrow.map { it.y })
        val widenedError = Statistics.rmse(truth.map { it.y }, widened.map { it.y })
        assertTrue(widenedError < narrowError)
    }

    @Test
    fun maximumSpanDistanceLimitsTheNeighborhood() {
        // A spike beyond the distance limit must not influence the point being smoothed
        val values = List(200) { Vector2(it.toFloat(), if (it == 150) 1000f else it * 2f) }

        val smoothed = LoessFilter2D(0.5f, 0, maximumSpanDistance = 20f).filter(values)

        assertEquals(40f, smoothed[20].y, 0.01f)
    }

    @Test
    fun maximumSpanDistanceDoesNotDependOnTheFirstXValue() {
        val values = List(400) { Vector2(it * 1.5f, profile(it) + (it % 7) * 0.3f) }
        val shifted = values.map { Vector2(it.x + 1024f, it.y) }
        val filter = LoessFilter2D(0.5f, 2, maximumSpanDistance = 40f)

        assertEquals(filter.filter(values).map { it.y }, filter.filter(shifted).map { it.y })
    }

    @Test
    fun maximumSpanDistanceDoesNotDependOnSamplingDensity() {
        val dense = List(2000) { Vector2(it * 0.001f, profile(it / 10)) }
        val sparse = dense.filterIndexed { i, _ -> i % 10 == 0 }
        val filter = LoessFilter2D(0.5f, 0, maximumSpanDistance = 0.1f)

        val denseSmoothed = filter.filter(dense)
        val sparseSmoothed = filter.filter(sparse)

        sparseSmoothed.forEachIndexed { i, point -> assertEquals(denseSmoothed[i * 10].y, point.y, 0.5f) }
    }

    @Test
    fun spanSizeLimitSmallerThanMinimumThrows() {
        val values = List(100) { Vector2(it.toFloat(), it.toFloat()) }

        assertThrows(IllegalArgumentException::class.java) {
            LoessFilter2D(0.1f, 1, minimumSpanSize = 20, maximumSpanSize = 10).filter(values)
        }
    }

    private fun profile(index: Int): Float {
        return 100f + 20f * sin(index * 0.03f)
    }
}
