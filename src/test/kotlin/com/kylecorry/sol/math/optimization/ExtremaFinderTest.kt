package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.Vector2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.math.sin

class ExtremaFinderTest {

    @ParameterizedTest
    @MethodSource("provideExtremas")
    fun simpleExtremaFinder(start: Double, end: Double, fn: (x: Double) -> Double, expected: List<Extremum>) {
        val step = 0.01
        val finder = SimpleExtremaFinder(step)
        val actual = finder.find(Range(start, end), fn)
        assertExtremasEqual(expected, actual, step.toFloat(), 0.01f)
    }

    @ParameterizedTest
    @MethodSource("provideExtremas")
    fun goldenSearchExtremaFinder(start: Double, end: Double, fn: (x: Double) -> Double, expected: List<Extremum>) {
        val step = 0.5
        val tolerance = 0.01
        val finder = GoldenSearchExtremaFinder(step, tolerance)
        val actual = finder.find(Range(start, end), fn)
        assertExtremasEqual(expected, actual, tolerance.toFloat() * 2, 0.01f)
    }

    @ParameterizedTest
    @MethodSource("provideExtremas")
    fun iterativeSimpleExtremaFinder(start: Double, end: Double, fn: (x: Double) -> Double, expected: List<Extremum>) {
        val initialStep = 0.5
        val finalStep = 0.01
        val finder = IterativeSimpleExtremaFinder(initialStep, finalStep, 3)
        val actual = finder.find(Range(start, end), fn)
        assertExtremasEqual(expected, actual, finalStep.toFloat() * 2, 0.01f)
    }

    private fun assertExtremasEqual(
        expected: List<Extremum>,
        actual: List<Extremum>,
        xTolerance: Float,
        yTolerance: Float
    ) {
        assert(expected.size == actual.size) { "Expected ${expected.size} extremas but found ${actual.size}" }
        for (i in expected.indices) {
            val expectedExtrema = expected[i]
            val actualExtrema = actual[i]
            assertEquals(actualExtrema.isHigh, expectedExtrema.isHigh)
            assertEquals(actualExtrema.point.x, expectedExtrema.point.x, xTolerance)
            assertEquals(actualExtrema.point.y, expectedExtrema.point.y, yTolerance)
        }
    }

    companion object {
        @JvmStatic
        fun provideExtremas(): Stream<Arguments> {
            return Stream.of(
                // Sin (min and max)
                Arguments.of(
                    0.0,
                    2 * Math.PI,
                    { x: Double -> sin(x) },
                    listOf(
                        Extremum(
                            Vector2(Math.PI.toFloat() / 2f, 1f),
                            true
                        ),
                        Extremum(
                            Vector2(3 * Math.PI.toFloat() / 2f, -1f),
                            false
                        )
                    )
                ),
                // Sin (max only)
                Arguments.of(
                    0.0,
                    Math.PI,
                    { x: Double -> sin(x) },
                    listOf(
                        Extremum(
                            Vector2(Math.PI.toFloat() / 2f, 1f),
                            true
                        )
                    )
                ),
                // Sin (min only)
                Arguments.of(
                    Math.PI,
                    2 * Math.PI,
                    { x: Double -> sin(x) },
                    listOf(
                        Extremum(
                            Vector2(3 * Math.PI.toFloat() / 2f, -1f),
                            false
                        )
                    )
                ),
                // Flat line
                Arguments.of(
                    0.0,
                    10.0,
                    { x: Double -> 1.0 },
                    emptyList<Extremum>()
                ),
                // Linear
                Arguments.of(
                    0.0,
                    10.0,
                    { x: Double -> x },
                    emptyList<Extremum>()
                ),
                // Parabola
                Arguments.of(
                    0.0,
                    10.0,
                    { x: Double -> x * x },
                    listOf(
                        Extremum(
                            Vector2(0f, 0f),
                            false
                        )
                    )
                ),
            )

        }
    }

}