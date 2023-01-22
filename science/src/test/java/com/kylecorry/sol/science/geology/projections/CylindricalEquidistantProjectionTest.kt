package com.kylecorry.sol.science.geology.projections

import assertk.assertThat
import assertk.assertions.isCloseTo
import com.kylecorry.sol.math.SolMath.toRadians
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.units.Coordinate
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import com.kylecorry.sol.tests.isCloseTo
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

internal class CylindricalEquidistantProjectionTest {

    @ParameterizedTest
    @MethodSource("provideToCoordinate")
    fun toCoordinate(x: Float, y: Float, expected: Coordinate) {
        val projection = CylindricalEquidistantProjection()
        val coordinate = projection.toCoordinate(Vector2(x, y))
        assertThat(coordinate).isCloseTo(expected, 0.5f)
    }

    @ParameterizedTest
    @MethodSource("provideToPixels")
    fun toPixels(coordinate: Coordinate, expectedX: Float, expectedY: Float) {
        val projection = CylindricalEquidistantProjection()
        val pixels = projection.toPixels(coordinate)
        assertThat(pixels.x).isCloseTo(expectedX, 0.5f)
        assertThat(pixels.y).isCloseTo(expectedY, 0.5f)
    }

    companion object {
        @JvmStatic
        fun provideToCoordinate(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0f, 0f, Coordinate.zero),
                Arguments.of(10f.toRadians(), 0f, Coordinate(0.0, 10.0)),
                Arguments.of((-10f).toRadians(), 0f, Coordinate(0.0, -10.0)),
                Arguments.of(0f, (-10f).toRadians(), Coordinate(-10.0, 0.0)),
                Arguments.of(0f, 10f.toRadians(), Coordinate(10.0, 0.0)),
                Arguments.of(10f.toRadians(), 10f.toRadians(), Coordinate(10.0, 10.0)),
                Arguments.of((-10f).toRadians(), (-10f).toRadians(), Coordinate(-10.0, -10.0)),
                Arguments.of(200f.toRadians(), 0f, Coordinate(0.0, -160.0)),
                Arguments.of((-200f).toRadians(), 0f, Coordinate(0.0, 160.0)),
                Arguments.of(0f, 100f.toRadians(), Coordinate(90.0, 0.0)),
                Arguments.of(0f, (-100f).toRadians(), Coordinate(-90.0, 0.0)),
            )
        }

        @JvmStatic
        fun provideToPixels(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Coordinate.zero, 0f, 0f),
                Arguments.of(Coordinate(0.0, 10.0), 10f.toRadians(), 0f),
                Arguments.of(Coordinate(0.0, -10.0), (-10f).toRadians(), 0f),
                Arguments.of(Coordinate(-10.0, 0.0), 0f, (-10f).toRadians()),
                Arguments.of(Coordinate(10.0, 0.0), 0f, 10f.toRadians()),
                Arguments.of(Coordinate(10.0, 10.0), 10f.toRadians(), 10f.toRadians()),
                Arguments.of(Coordinate(-10.0, -10.0), (-10f).toRadians(), (-10f).toRadians()),
                Arguments.of(Coordinate(0.0, -160.0), (-160f).toRadians(), 0f),
                Arguments.of(Coordinate(0.0, 160.0), 160f.toRadians(), 0f),
                Arguments.of(Coordinate(90.0, 0.0), 0f, 100f.toRadians()),
                Arguments.of(Coordinate(-90.0, 0.0), 0f, (-100f).toRadians()),
            )
        }
    }

}