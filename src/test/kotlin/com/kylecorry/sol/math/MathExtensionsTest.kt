package com.kylecorry.sol.math

import com.kylecorry.sol.math.MathExtensions.negative
import com.kylecorry.sol.math.MathExtensions.positive
import com.kylecorry.sol.math.MathExtensions.real
import com.kylecorry.sol.math.MathExtensions.round
import com.kylecorry.sol.math.MathExtensions.roundNearest
import com.kylecorry.sol.math.MathExtensions.roundNearestAngle
import com.kylecorry.sol.math.MathExtensions.roundPlaces
import com.kylecorry.sol.math.MathExtensions.toDegrees
import com.kylecorry.sol.math.MathExtensions.toRadians
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class MathExtensionsTest {

    @ParameterizedTest
    @MethodSource("provideRadians")
    fun toRadiansDouble(angle: Double, expected: Double) {
        val actual = angle.toRadians()
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideRadians")
    fun toRadiansFloat(angle: Double, expected: Double) {
        val actual = angle.toFloat().toRadians()
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideRadians")
    fun toDegreesDouble(expected: Double, angle: Double) {
        val actual = angle.toDegrees()
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideRadians")
    fun toDegreesFloat(expected: Double, angle: Double) {
        val actual = angle.toFloat().toDegrees()
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideRoundPlaces")
    fun roundPlacesDouble(value: Double, places: Int, expected: Double) {
        val actual = value.roundPlaces(places)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideRoundPlaces")
    fun roundPlacesFloat(value: Double, places: Int, expected: Double) {
        val actual = value.toFloat().roundPlaces(places)
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    @ParameterizedTest
    @CsvSource(
        "0.0, 1.0, 1.0",
        "1.0, 0.0, 1.0",
        "-1.0, 0.0, 1.0",
        "NaN, 1.0, NaN",
        "Infinity, 1.0, Infinity",
        "-Infinity, 1.0, Infinity"
    )
    fun positive(value: Float, zeroReplacement: Float, expected: Float) {
        val actual = value.positive(zeroReplacement)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource(
        "0.0, -1.0, -1.0",
        "1.0, 0.0, -1.0",
        "-1.0, 0.0, -1.0",
        "NaN, -1.0, NaN",
        "Infinity, -1.0, -Infinity",
        "-Infinity, -1.0, -Infinity"
    )
    fun negative(value: Float, zeroReplacement: Float, expected: Float) {
        val actual = value.negative(zeroReplacement)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource(
        "NaN, 1.0, 1.0",
        "Infinity, 1.0, 1.0",
        "-Infinity, 1.0, 1.0",
        "0.0, 1.0, 0.0",
        "1.0, 0.0, 1.0",
        "-1.0, 0.0, -1.0"
    )
    fun real(value: Float, defaultValue: Float, expected: Float) {
        val actual = value.real(defaultValue)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource(
        "1.0, 1.0, 1.0",
        "1.5, 1.0, 2.0",
        "1.4, 1.0, 1.0",
        "20.0, 10.0, 20.0",
        "20.0, 15.0, 15.0",
        "25.0, 15.0, 30.0",
        "0.0, 1.0, 0.0",
        "-1.0, 1.0, -1.0",
        "-1.5, 1.0, -1.0",
        "-1.6, 1.0, -2.0",
    )
    fun roundNearestDouble(value: Double, nearest: Double, expected: Double) {
        val actual = value.roundNearest(nearest)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @CsvSource(
        "1.0, 1.0, 1.0",
        "1.5, 1.0, 2.0",
        "1.4, 1.0, 1.0",
        "20.0, 10.0, 20.0",
        "20.0, 15.0, 15.0",
        "25.0, 15.0, 30.0",
        "0.0, 1.0, 0.0",
        "-1.0, 1.0, -1.0",
        "-1.5, 1.0, -1.0",
        "-1.6, 1.0, -2.0",
    )
    fun roundNearestFloat(value: Float, nearest: Float, expected: Float) {
        val actual = value.roundNearest(nearest)
        assertEquals(expected, actual, 0.00001f)
    }

    @ParameterizedTest
    @CsvSource(
        "1, 1, 1",
        "2, 1, 2",
        "0, 2, 0",
        "3, 2, 4",
        "20, 10, 20",
        "20, 16, 16",
        "25, 16, 32",
        "-1, 1, -1",
        "-2, 5, 0",
        "-3, 5, -5",
    )
    fun roundNearestInt(value: Int, nearest: Int, expected: Int) {
        val actual = value.roundNearest(nearest)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource(
        "0.0, 15.0, 0.0",
        "355.0, 15.0, 0.0",
        "5.0, 15.0, 0.0",
        "10.0, 15.0, 15.0",
        "20.0, 15.0, 15.0",
        "25.0, 15.0, 30.0",
        "-15.0, 15.0, 345.0",
        "-20.0, 15.0, 345.0",
        "-25.0, 15.0, 330.0",
    )
    fun roundNearestAngleFloat(value: Float, nearest: Float, expected: Float) {
        val actual = value.roundNearestAngle(nearest)
        assertEquals(expected, actual, 0.00001f)
    }

    @ParameterizedTest
    @CsvSource(
        "0.0, 15.0, 0.0",
        "355.0, 15.0, 0.0",
        "5.0, 15.0, 0.0",
        "10.0, 15.0, 15.0",
        "20.0, 15.0, 15.0",
        "25.0, 15.0, 30.0",
        "-15.0, 15.0, 345.0",
        "-20.0, 15.0, 345.0",
        "-25.0, 15.0, 330.0",
    )
    fun roundNearestAngleDouble(value: Double, nearest: Double, expected: Double) {
        val actual = value.roundNearestAngle(nearest)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @CsvSource(
        "AwayFromZero, 1.0, 1",
        "AwayFromZero, 1.1, 1",
        "AwayFromZero, 1.5, 2",
        "AwayFromZero, 1.9, 2",
        "AwayFromZero, -1.0, -1",
        "AwayFromZero, -1.1, -1",
        "AwayFromZero, -1.5, -2",
        "AwayFromZero, -1.9, -2",
        "TowardZero, 1.0, 1",
        "TowardZero, 1.1, 1",
        "TowardZero, 1.5, 1",
        "TowardZero, 1.9, 2",
        "TowardZero, -1.0, -1",
        "TowardZero, -1.1, -1",
        "TowardZero, -1.5, -1",
        "TowardZero, -1.9, -2",
    )
    fun round(method: RoundingMethod, value: Float, expected: Int) {
        val actual = value.round(method)
        assertEquals(expected, actual)
    }

    companion object {

        @JvmStatic
        fun provideRadians(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(45.0, Math.toRadians(45.0)),
                Arguments.of(90.0, Math.toRadians(90.0)),
                Arguments.of(0.0, Math.toRadians(0.0)),
                Arguments.of(-90.0, Math.toRadians(-90.0)),
                Arguments.of(720.0, Math.toRadians(720.0)),
            )
        }

        @JvmStatic
        fun provideRoundPlaces(): Stream<Arguments> {
            return Stream.of(
                // Floor
                Arguments.of(1.1111, 0, 1.0),
                Arguments.of(1.1111, 1, 1.1),
                Arguments.of(1.1111, 2, 1.11),
                Arguments.of(1.1111, 3, 1.111),
                Arguments.of(1.1111, 4, 1.1111),
                Arguments.of(1.1111, 5, 1.1111),

                // Ceil
                Arguments.of(1.6666, 0, 2.0),
                Arguments.of(1.6666, 1, 1.7),
                Arguments.of(1.6666, 2, 1.67),
                Arguments.of(1.6666, 3, 1.667),
                Arguments.of(1.6666, 4, 1.6666),
                Arguments.of(1.6666, 5, 1.6666),

                // Middle
                Arguments.of(1.5555, 0, 2.0),
                Arguments.of(1.5555, 1, 1.6),
                Arguments.of(1.5555, 2, 1.56),
                Arguments.of(1.5555, 3, 1.556),
                Arguments.of(1.5555, 4, 1.5555),
                Arguments.of(1.5555, 5, 1.5555),

                // Negative
                Arguments.of(15.11, -1, 20.0),
                Arguments.of(15.11, -2, 0.0),
                Arguments.of(55.11, -2, 100.0),
                Arguments.of(155.11, -2, 200.0),

                // Large
                Arguments.of(8000000.0, 5, 8000000.0),
                Arguments.of(8000000.125555, 5, 8000000.12556),
                Arguments.of(8000000.125555555555, 8, 8000000.12555556),
            )
        }
    }
}
