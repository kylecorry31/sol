package com.kylecorry.sol.science.geology

import assertk.assertThat
import assertk.assertions.isCloseTo
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

    @ParameterizedTest
    @MethodSource("provideFrom")
    fun from(
        points: List<Coordinate>,
        checkForFullWorld: Boolean,
        expected: CoordinateBounds
    ) {
        val bounds = CoordinateBounds.from(points, checkForFullWorld)
        assertThat(bounds.north, name = "north").isCloseTo(expected.north, 0.00001)
        assertThat(bounds.south, name = "south").isCloseTo(expected.south, 0.00001)
        assertThat(bounds.east, name = "east").isCloseTo(expected.east, 0.00001)
        assertThat(bounds.west, name = "west").isCloseTo(expected.west, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideGrow")
    fun grow(bounds: CoordinateBounds, percent: Float, expected: CoordinateBounds) {
        val grown = bounds.grow(percent)
        assertThat(grown.north, name = "north").isCloseTo(expected.north, 0.00001)
        assertThat(grown.south, name = "south").isCloseTo(expected.south, 0.00001)
        // Longitudes may wrap; compare width and center longitude for robustness
        assertThat(grown.widthDegrees(), name = "widthDegrees").isCloseTo(expected.widthDegrees(), 0.00001)
        assertThat(grown.center.longitude, name = "centerLongitude").isCloseTo(expected.center.longitude, 0.00001)
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

        @JvmStatic
        fun provideGrow(): Stream<Arguments> {
            return Stream.of(
                // Non-wrap rectangle, 10x10 -> grow 10% => lat +/-1, lon +/-1
                Arguments.of(
                    CoordinateBounds(10.0, 10.0, 0.0, 0.0),
                    0.1f,
                    CoordinateBounds(11.0, 11.0, -1.0, -1.0)
                ),

                // Zero width rectangle, widthDegrees = 0 -> no horizontal change
                Arguments.of(
                    CoordinateBounds(10.0, 0.0, 0.0, 0.0),
                    0.5f,
                    CoordinateBounds(15.0, 0.0, -5.0, 0.0)
                ),

                // Zero height rectangle, heightDegrees = 0 -> no vertical change
                Arguments.of(
                    CoordinateBounds(0.0, 10.0, 0.0, 0.0),
                    0.5f,
                    CoordinateBounds(0.0, 15.0, 0.0, -5.0)
                ),

                // Wrap across antimeridian: west=170, east=-170, width=20 -> 50% grow => lon +/-10
                Arguments.of(
                    CoordinateBounds(10.0, -170.0, 0.0, 170.0),
                    0.5f,
                    CoordinateBounds(15.0, -160.0, -5.0, 160.0)
                ),

                // Near poles: clamp lat to +/-90
                Arguments.of(
                    CoordinateBounds(89.0, 10.0, 80.0, 0.0),
                    0.5f, // height=9 -> latDelta=4.5 => north would be 93.5 -> clamp to 90
                    CoordinateBounds(90.0, 15.0, 75.5, -5.0)
                ),

                // World bounds remain world after grow
                Arguments.of(
                    CoordinateBounds.world,
                    0.25f,
                    CoordinateBounds.world
                ),

                // Percent = 0 -> no change
                Arguments.of(
                    CoordinateBounds(10.0, 10.0, 0.0, 0.0),
                    0.0f,
                    CoordinateBounds(10.0, 10.0, 0.0, 0.0)
                ),

                // East touches antimeridian, growth crosses it
                Arguments.of(
                    CoordinateBounds(10.0, 180.0, 0.0, 179.0),
                    0.5f, // width=1 -> lonDelta=0.5; height=10 -> latDelta=5
                    CoordinateBounds(15.0, -179.5, -5.0, 178.5)
                ),

                // West touches antimeridian, growth crosses it
                Arguments.of(
                    CoordinateBounds(10.0, -179.0, 0.0, -180.0),
                    0.5f,
                    CoordinateBounds(15.0, -178.5, -5.0, 179.5)
                ),
            )
        }

        @JvmStatic
        fun provideFrom(): Stream<Arguments> {
            return Stream.of(
                // Full world exact extremes with check enabled
                Arguments.of(
                    listOf(
                        Coordinate(10.0, -180.0),
                        Coordinate(10.0, 180.0),
                        Coordinate(-10.0, -180.0),
                        Coordinate(-10.0, 180.0)
                    ),
                    true,
                    CoordinateBounds(10.0, 180.0, -10.0, -180.0)
                ),

                // Approximate extremes with check enabled: use provided min/max longitudes
                Arguments.of(
                    listOf(
                        Coordinate(5.0, -180.0),
                        Coordinate(5.0, 179.9995),
                        Coordinate(-5.0, -180.0),
                        Coordinate(-5.0, 179.9995)
                    ),
                    true,
                    CoordinateBounds(5.0, 179.9995, -5.0, -180.0)
                ),

                // Approximate extremes with check disabled: minimal wrap across antimeridian
                Arguments.of(
                    listOf(
                        Coordinate(5.0, -180.0),
                        Coordinate(5.0, 179.9995),
                        Coordinate(-5.0, -180.0),
                        Coordinate(-5.0, 179.9995)
                    ),
                    false,
                    CoordinateBounds(5.0, -180.0, -5.0, 179.9995)
                ),

                // Exact extremes with check disabled: zero width, center at -180
                Arguments.of(
                    listOf(
                        Coordinate(10.0, -180.0),
                        Coordinate(10.0, 180.0),
                        Coordinate(-10.0, -180.0),
                        Coordinate(-10.0, 180.0)
                    ),
                    false,
                    CoordinateBounds(10.0, -180.0, -10.0, -180.0)
                ),

                // Non-wrap mid-longitudes: west=-20, east=30
                Arguments.of(
                    listOf(
                        Coordinate(10.0, -20.0),
                        Coordinate(10.0, 30.0),
                        Coordinate(-10.0, -20.0),
                        Coordinate(-10.0, 30.0)
                    ),
                    false,
                    CoordinateBounds(10.0, 30.0, -10.0, -20.0)
                ),

                // Wrap across antimeridian: west=170, east=-170
                Arguments.of(
                    listOf(
                        Coordinate(10.0, 170.0),
                        Coordinate(10.0, -170.0),
                        Coordinate(-10.0, 170.0),
                        Coordinate(-10.0, -170.0)
                    ),
                    false,
                    CoordinateBounds(10.0, -170.0, -10.0, 170.0)
                ),
            )
        }

    }
}