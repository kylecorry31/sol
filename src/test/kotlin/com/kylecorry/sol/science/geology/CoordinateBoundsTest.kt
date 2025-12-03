package com.kylecorry.sol.science.geology

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.kylecorry.sol.tests.isCloseTo
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.CoordinateLongConverter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class CoordinateBoundsTest {

    @ParameterizedTest
    @MethodSource("provideContains")
    fun contains(bounds: CoordinateBounds, @ConvertWith(CoordinateLongConverter::class) coordinate: Coordinate, expected: Boolean) {
        assertThat(bounds.contains(coordinate)).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("provideCenter")
    fun center(bounds: CoordinateBounds, @ConvertWith(CoordinateLongConverter::class) expected: Coordinate) {
        assertThat(bounds.center).isCloseTo(expected, 1f)
    }

    @ParameterizedTest
    @MethodSource("provideIntersects")
    fun intersects(
        bounds1: CoordinateBounds,
        bounds2: CoordinateBounds,
        expected: Boolean
    ) {
        assertThat(bounds1.intersects(bounds2), name = "intersects(b1=$bounds1, b2=$bounds2)").isEqualTo(expected)
        assertThat(bounds2.intersects(bounds1), name = "intersects(b1=$bounds2, b2=$bounds1)").isEqualTo(expected)
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

        @JvmStatic
        fun provideIntersects(): Stream<Arguments> {
            return Stream.of(
                // Basic non-wrap overlap
                Arguments.of(
                    CoordinateBounds(10.0, 10.0, 0.0, 0.0),
                    CoordinateBounds(5.0, 5.0, -5.0, -5.0),
                    true
                ),

                // Disjoint vertically (no overlap in latitude)
                Arguments.of(
                    CoordinateBounds(10.0, 10.0, 5.0, 0.0),
                    CoordinateBounds(4.0, 10.0, 0.0, 0.0),
                    false
                ),

                // Touching at latitude edge (should intersect)
                Arguments.of(
                    CoordinateBounds(10.0, 10.0, 0.0, 0.0),
                    CoordinateBounds(0.0, 10.0, -5.0, 0.0),
                    true
                ),

                // Disjoint horizontally (non-wrap)
                Arguments.of(
                    CoordinateBounds(10.0, 10.0, 0.0, 0.0),
                    CoordinateBounds(10.0, 30.0, 0.0, 20.0),
                    false
                ),

                // Touching at longitude edge (non-wrap, should intersect)
                Arguments.of(
                    CoordinateBounds(10.0, 10.0, 0.0, 0.0),
                    CoordinateBounds(10.0, 20.0, 0.0, 10.0),
                    true
                ),

                // Self wraps (antimeridian) intersects on left segment [-180, east]
                Arguments.of(
                    CoordinateBounds(10.0, -170.0, 0.0, 170.0),
                    CoordinateBounds(10.0, -172.0, 0.0, -178.0),
                    true
                ),

                // Self wraps intersects on right segment [west, 180]
                Arguments.of(
                    CoordinateBounds(10.0, -170.0, 0.0, 170.0),
                    CoordinateBounds(10.0, 175.0, 0.0, 172.0),
                    true
                ),

                // Self wraps but disjoint horizontally
                Arguments.of(
                    CoordinateBounds(10.0, -170.0, 0.0, 170.0),
                    CoordinateBounds(10.0, -150.0, 0.0, -160.0),
                    false
                ),

                // Other wraps vs non-wrap intersect (overlap on left segment)
                Arguments.of(
                    CoordinateBounds(10.0, -165.0, 0.0, -175.0),
                    CoordinateBounds(10.0, -170.0, 0.0, 170.0),
                    true
                ),

                // Other wraps vs non-wrap disjoint
                Arguments.of(
                    CoordinateBounds(10.0, -150.0, 0.0, -160.0),
                    CoordinateBounds(10.0, -170.0, 0.0, 170.0),
                    false
                ),

                // Both wrap with vertical overlap -> should intersect
                Arguments.of(
                    CoordinateBounds(10.0, -170.0, 0.0, 170.0),
                    CoordinateBounds(10.0, -160.0, 0.0, 160.0),
                    true
                ),

                // Both wrap but vertically disjoint -> should not intersect
                Arguments.of(
                    CoordinateBounds(10.0, -170.0, 5.0, 170.0),
                    CoordinateBounds(4.0, -160.0, 0.0, 160.0),
                    false
                ),

                // Intersection with world bounds (should intersect whenever lat overlaps)
                Arguments.of(
                    CoordinateBounds.world,
                    CoordinateBounds(10.0, 10.0, 0.0, 0.0),
                    true
                ),

                // Zero-width rectangle intersecting a normal rectangle (touching by longitude)
                Arguments.of(
                    CoordinateBounds(1.0, 0.0, 0.0, 0.0),
                    CoordinateBounds(1.0, 1.0, -1.0, -1.0),
                    true
                ),

                // Zero-height rectangle intersecting a normal rectangle (touching by latitude)
                Arguments.of(
                    CoordinateBounds(0.0, 10.0, 0.0, -10.0),
                    CoordinateBounds(1.0, 1.0, -1.0, -1.0),
                    true
                ),

                // Zero-width vs zero-width at same longitude -> intersect
                Arguments.of(
                    CoordinateBounds(1.0, 10.0, -1.0, 10.0),
                    CoordinateBounds(1.0, 10.0, -1.0, 10.0),
                    true
                ),

                // Zero-width vs zero-width at different longitudes -> no intersect
                Arguments.of(
                    CoordinateBounds(1.0, 10.0, -1.0, 10.0),
                    CoordinateBounds(1.0, 20.0, -1.0, 20.0),
                    false
                ),

                // Touching exactly at antimeridian with non-wrap other reaching 180
                Arguments.of(
                    CoordinateBounds(10.0, -170.0, 0.0, 170.0),
                    CoordinateBounds(10.0, 180.0, 0.0, 170.0),
                    true
                ),

                // Non-wrap near -180 touching wrapped on left segment
                Arguments.of(
                    CoordinateBounds(10.0, -180.0, 0.0, -180.0),
                    CoordinateBounds(10.0, -179.0, 0.0, 179.0),
                    true
                ),

                // Basically full world
                Arguments.of(
                    CoordinateBounds(10.0, 179.99995, 0.0, -180.0),
                    CoordinateBounds(5.0, 180.0, -5.0, 179.99997),
                    true
                )
            )
        }

    }
}