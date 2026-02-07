package com.kylecorry.sol.math.analysis

import com.kylecorry.sol.math.trigonometry.CosineWave
import com.kylecorry.sol.math.trigonometry.Trigonometry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

internal class TrigonometryTest {

    @ParameterizedTest
    @CsvSource(
        "0, 0, true, 0",
        "0, 0, false, 0",
        "0, 90, true, 270",
        "0, 90, false, 90",
        "90, 0, true, 90",
        "90, 0, false, 270",
        "90, 90, true, 0",
        "90, 90, false, 0",
        "180, 0, true, 180",
        "180, 0, false, 180",
        "180, 90, true, 90",
        "180, 90, false, 270",
        "270, 0, true, 270",
        "270, 0, false, 90",
        "270, 90, true, 180",
        "270, 90, false, 180",
        "90, 360, true, 90",
        "90, 360, false, 270",
    )
    fun remapUnitAngle(
        originalAngle: Float,
        start: Float,
        isCounterClockwise: Boolean,
        expected: Float
    ) {
        val actual = Trigonometry.remapUnitAngle(originalAngle, start, isCounterClockwise)
        assertEquals(expected, actual, 0.0001f)
    }


    @ParameterizedTest
    @CsvSource(
        "0, 0, true, 0",
        "0, 0, false, 0",
        "0, 90, true, 90",
        "0, 90, false, 90",
        "90, 0, true, 90",
        "90, 0, false, 270",
        "90, 90, true, 180",
        "90, 90, false, 0",
        "180, 0, true, 180",
        "180, 0, false, 180",
        "180, 90, true, 270",
        "180, 90, false, 270",
        "270, 0, true, 270",
        "270, 0, false, 90",
        "270, 90, true, 0",
        "270, 90, false, 180",
        "90, 360, true, 90",
        "90, 360, false, 270",
        "450, 90, true, 180",
    )
    fun toUnitAngle(
        originalAngle: Float,
        start: Float,
        isCounterClockwise: Boolean,
        expected: Float
    ) {
        val actual = Trigonometry.toUnitAngle(originalAngle, start, isCounterClockwise)
        assertEquals(expected, actual, 0.0001f)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0, 0",
        "0, 1, 1, 1",
        "1, 0, -1, 1",
        "1, 1, 0, 2",
    )
    fun getRangeY(amplitude: Float, verticalOffset: Float, expectedMin: Float, expectedMax: Float) {
        val wave = CosineWave(amplitude, 1f, 0f, verticalOffset)
        val range = Trigonometry.getRangeY(wave)
        assertEquals(expectedMin, range.start, 0.0001f)
        assertEquals(expectedMax, range.end, 0.0001f)
    }

    @Test
    fun getCombinationRangeY() {
        val waves = listOf(
            CosineWave(1f, 1f, 0f, 0f),
            CosineWave(2f, 3f, 1f, 1f),
            CosineWave(3f, 2f, 0f, 0f)
        )

        val range = Trigonometry.getCombinationRangeY(waves)

        assertEquals(-5f, range.start, 0.0001f)
        assertEquals(7f, range.end, 0.0001f)
    }

    @Test
    fun normalizeAngle() {
        assertEquals(0.0, Trigonometry.normalizeAngle(0.0), 0.0)
        assertEquals(180.0, Trigonometry.normalizeAngle(180.0), 0.0)
        assertEquals(0.0, Trigonometry.normalizeAngle(0.0), 0.0)
        assertEquals(1.0, Trigonometry.normalizeAngle(361.0), 0.0)
        assertEquals(359.0, Trigonometry.normalizeAngle(-1.0), 0.0)
        assertEquals(180.0, Trigonometry.normalizeAngle(-180.0), 0.0)
        assertEquals(0.0, Trigonometry.normalizeAngle(720.0), 0.0)
    }

    @ParameterizedTest
    @MethodSource("provideDeltaAngle")
    fun deltaAngle(angle1: Float, angle2: Float, expected: Float) {
        val actual = Trigonometry.deltaAngle(angle1, angle2)
        assertEquals(expected, actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideTanDegrees")
    fun tanDegreesDouble(angle: Double, expected: Double) {
        val actual = Trigonometry.tanDegrees(angle)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideTanDegrees")
    fun tanDegreesFloat(angle: Double, expected: Double) {
        val actual = Trigonometry.tanDegrees(angle.toFloat())
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideSinDegrees")
    fun sinDegreesDouble(angle: Double, expected: Double) {
        val actual = Trigonometry.sinDegrees(angle)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideSinDegrees")
    fun sinDegreesFloat(angle: Double, expected: Double) {
        val actual = Trigonometry.sinDegrees(angle.toFloat())
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideCosDegrees")
    fun cosDegreesDouble(angle: Double, expected: Double) {
        val actual = Trigonometry.cosDegrees(angle)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideCosDegrees")
    fun cosDegreesFloat(angle: Double, expected: Double) {
        val actual = Trigonometry.cosDegrees(angle.toFloat())
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    companion object {
        @JvmStatic
        fun provideDeltaAngle(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0.0f, 0.0f, 0.0f),
                Arguments.of(15.0f, 15.0f, 0.0f),
                Arguments.of(15.0f, 25.0f, 10.0f),
                Arguments.of(25.0f, 15.0f, -10.0f),
                Arguments.of(0.0f, 360.0f, 0.0f),
                Arguments.of(0.0f, 720.0f, 0.0f),
                Arguments.of(10.0f, 370.0f, 0.0f),
                Arguments.of(-10.0f, 370.0f, 20.0f),
                Arguments.of(370.0f, -10.0f, -20.0f),
                Arguments.of(10.0f, 180.0f, 170.0f),
                Arguments.of(-10.0f, 180.0f, -170.0f),
                Arguments.of(0.0f, 180.0f, 180.0f),
            )
        }

        @JvmStatic
        fun provideTanDegrees(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(45.0, tan(Math.toRadians(45.0))),
                Arguments.of(80.0, tan(Math.toRadians(80.0))),
                Arguments.of(0.0, tan(Math.toRadians(0.0))),
                Arguments.of(-80.0, tan(Math.toRadians(-80.0))),
                Arguments.of(720.0, tan(Math.toRadians(720.0))),
            )
        }

        @JvmStatic
        fun provideSinDegrees(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(45.0, sin(Math.toRadians(45.0))),
                Arguments.of(90.0, sin(Math.toRadians(90.0))),
                Arguments.of(0.0, sin(Math.toRadians(0.0))),
                Arguments.of(-90.0, sin(Math.toRadians(-90.0))),
                Arguments.of(720.0, sin(Math.toRadians(720.0))),
            )
        }

        @JvmStatic
        fun provideCosDegrees(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(45.0, cos(Math.toRadians(45.0))),
                Arguments.of(90.0, cos(Math.toRadians(90.0))),
                Arguments.of(0.0, cos(Math.toRadians(0.0))),
                Arguments.of(-90.0, cos(Math.toRadians(-90.0))),
                Arguments.of(720.0, cos(Math.toRadians(720.0))),
            )
        }
    }
}