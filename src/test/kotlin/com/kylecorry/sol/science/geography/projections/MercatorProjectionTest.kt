package com.kylecorry.sol.science.geography.projections

import assertk.assertThat
import assertk.assertions.isCloseTo
import com.kylecorry.sol.math.SolMath.toRadians
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.tests.isCloseTo
import com.kylecorry.sol.units.Coordinate
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.math.PI

internal class MercatorProjectionTest {

    @ParameterizedTest
    @MethodSource("provideToCoordinate")
    fun toCoordinate(x: Float, y: Float, expected: Coordinate) {
        val projection = MercatorProjection()
        val coordinate = projection.toCoordinate(Vector2(x, y))
        assertThat(coordinate).isCloseTo(expected, 0.5f)
    }

    @ParameterizedTest
    @MethodSource("provideToPixels")
    fun toPixels(coordinate: Coordinate, expectedX: Float, expectedY: Float) {
        val projection = MercatorProjection()
        val pixels = projection.toPixels(coordinate)
        assertThat(pixels.x).isCloseTo(expectedX, 0.5f)
        assertThat(pixels.y).isCloseTo(expectedY, 0.5f)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 1",
        "90, 0",
        "45, 0.7071068",
        "-45, 0.7071068",
        "30, 0.8660254",
        "-30, 0.8660254",
        "60, 0.5",
        "-60, 0.5",
        "80, 0.1736482",
        "-80, 0.1736482",
    )
    fun getScaleForLatitude(latitude: Double, expected: Float) {
        val scale = MercatorProjection.getScaleForLatitude(latitude)
        assertThat(scale).isCloseTo(expected, 0.0001f)
    }

    companion object {
        @JvmStatic
        fun provideToCoordinate(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0f, 0f, Coordinate.zero),
                Arguments.of(0f, 0.17542583f, Coordinate(10.0, 0.0)),
                Arguments.of(0f, -0.17542583f, Coordinate(-10.0, 0.0)),
                Arguments.of(0f, 2.4362462f, Coordinate(80.0, 0.0)),
                Arguments.of(0f, -2.4362462f, Coordinate(-80.0, 0.0)),
                Arguments.of(10f.toRadians(), 0f, Coordinate(0.0, 10.0)),
                Arguments.of((-10f).toRadians(), 0f, Coordinate(0.0, -10.0)),
                Arguments.of(10f.toRadians(), 0.17542583f, Coordinate(10.0, 10.0)),
                Arguments.of((-10f).toRadians(), -0.17542583f, Coordinate(-10.0, -10.0)),
                Arguments.of(10f.toRadians(), 2.4362462f, Coordinate(80.0, 10.0)),
                Arguments.of((-10f).toRadians(), -2.4362462f, Coordinate(-80.0, -10.0)),
                Arguments.of(0f, 200f, Coordinate(90.0, 0.0)),
                Arguments.of(0f, -200f, Coordinate(-90.0, 0.0)),
                Arguments.of(0f, PI.toFloat(), Coordinate(85.051129, 0.0)),
                Arguments.of(0f, -PI.toFloat(), Coordinate(-85.051129, 0.0)),
                Arguments.of(200f.toRadians(), 0f, Coordinate(0.0, -160.0)),
                Arguments.of((-200f).toRadians(), 0f, Coordinate(0.0, 160.0)),
            )
        }

        @JvmStatic
        fun provideToPixels(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Coordinate.zero, 0f, 0f),
                Arguments.of(Coordinate(10.0, 0.0), 0f, 0.17542583f),
                Arguments.of(Coordinate(-10.0, 0.0), 0f, -0.17542583f),
                Arguments.of(Coordinate(80.0, 0.0), 0f, 2.4362462f),
                Arguments.of(Coordinate(-80.0, 0.0), 0f, -2.4362462f),
                Arguments.of(Coordinate(0.0, 10.0), 10f.toRadians(), 0f),
                Arguments.of(Coordinate(0.0, -10.0), (-10f).toRadians(), 0f),
                Arguments.of(Coordinate(10.0, 10.0), 10f.toRadians(), 0.17542583f),
                Arguments.of(Coordinate(-10.0, -10.0), (-10f).toRadians(), -0.17542583f),
                Arguments.of(Coordinate(80.0, 10.0), 10f.toRadians(), 2.4362462f),
                Arguments.of(Coordinate(-80.0, -10.0), (-10f).toRadians(), -2.4362462f),
                Arguments.of(Coordinate(90.0, 0.0), 0f, Float.POSITIVE_INFINITY),
                Arguments.of(Coordinate(-90.0, 0.0), 0f, Float.NEGATIVE_INFINITY),
                Arguments.of(Coordinate(-85.051129, 0.0), 0f, -PI.toFloat()),
                Arguments.of(Coordinate(85.051129, 0.0), 0f, PI.toFloat()),
                Arguments.of(Coordinate(0.0, 160.0), 160f.toRadians(), 0f),
                Arguments.of(Coordinate(0.0, -160.0), (-160f).toRadians(), 0f),
            )
        }
    }

}