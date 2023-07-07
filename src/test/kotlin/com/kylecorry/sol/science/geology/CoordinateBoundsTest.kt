package com.kylecorry.sol.science.geology

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.kylecorry.sol.tests.isCloseTo
import com.kylecorry.sol.units.Coordinate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class CoordinateBoundsTest {

    @ParameterizedTest
    @MethodSource("provideContains")
    fun contains(bounds: CoordinateBounds, coordinate: Coordinate, expected: Boolean) {
        assertThat(bounds.contains(coordinate)).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("provideCenter")
    fun center(bounds: CoordinateBounds, expected: Coordinate) {
        assertThat(bounds.center).isCloseTo(expected, 1f)
    }

    companion object {

        @JvmStatic
        fun provideCenter(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(CoordinateBounds.empty, Coordinate.zero),
                Arguments.of(CoordinateBounds(1.0, 10.0, -1.0, -10.0), Coordinate.zero),
                Arguments.of(CoordinateBounds(1.0, 8.0, 0.0, -10.0), Coordinate(0.5, -1.0)),
                Arguments.of(CoordinateBounds(1.0, -10.0, 0.0, 10.0), Coordinate(0.5, 180.0)),
                Arguments.of(CoordinateBounds(80.0, 170.0, -20.0, -160.0), Coordinate(30.0, 5.0)),
                Arguments.of(CoordinateBounds(80.0, -170.0, -20.0, 160.0), Coordinate(30.0, 175.0)),
            )
        }

        @JvmStatic
        fun provideContains(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    CoordinateBounds.empty, Coordinate(1.0, 2.0), false
                ),
                Arguments.of(
                    CoordinateBounds(1.0, 10.0, -1.0, -10.0), Coordinate(1.0, 2.0), true
                ),
                Arguments.of(
                    CoordinateBounds(1.0, 10.0, -1.0, -10.0), Coordinate(2.0, 2.0), false
                ),
                Arguments.of(
                    CoordinateBounds(1.0, 10.0, -1.0, -10.0), Coordinate(-2.0, 2.0), false
                ),
                Arguments.of(
                    CoordinateBounds(1.0, 10.0, -1.0, -10.0), Coordinate(1.0, 11.0), false
                ),
                Arguments.of(
                    CoordinateBounds(1.0, 10.0, -1.0, -10.0), Coordinate(1.0, -11.0), false
                ),
                Arguments.of(
                    CoordinateBounds(1.0, -10.0, -1.0, 10.0), Coordinate(1.0, 2.0), false
                ),
                Arguments.of(
                    CoordinateBounds(1.0, -170.0, -1.0, 170.0), Coordinate(1.0, 180.0), true
                ),
                Arguments.of(
                    CoordinateBounds(1.0, -170.0, -1.0, 170.0), Coordinate(1.0, -180.0), true
                )
            )
        }

    }
}