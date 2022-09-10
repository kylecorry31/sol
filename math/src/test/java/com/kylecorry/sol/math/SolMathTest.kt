package com.kylecorry.sol.math

import com.kylecorry.sol.math.SolMath.roundPlaces
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.toRadians
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class SolMathTest {

    @ParameterizedTest
    @MethodSource("provideWrapDouble")
    fun wrapDouble(value: Double, min: Double, max: Double, expected: Double) {
        val actual = SolMath.wrap(value, min, max)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideWrapFloat")
    fun wrapFloat(value: Float, min: Float, max: Float, expected: Float) {
        val actual = SolMath.wrap(value, min, max)
        assertEquals(expected, actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("providePower")
    fun power(value: Double, exponent: Int, expected: Double) {
        val actual = SolMath.power(value, exponent)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("providePowerInt")
    fun power(value: Int, exponent: Int, expected: Int) {
        val actual = SolMath.power(value, exponent)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @MethodSource("providePolynomial")
    fun polynomial(x: Double, coefs: DoubleArray, expected: Double) {
        val actual = SolMath.polynomial(x, *coefs)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideCube")
    fun cube(x: Double, expected: Double) {
        val actual = SolMath.cube(x)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideSquare")
    fun square(x: Double, expected: Double) {
        val actual = SolMath.square(x)
        assertEquals(expected, actual, 0.00001)
    }

    @Test
    fun interpolate() {
        assertEquals(
            0.876125,
            SolMath.interpolate(0.18125, 0.884226, 0.877366, 0.870531),
            0.0000005
        )
    }

    @Test
    fun normalizeAngle() {
        assertEquals(0.0, SolMath.normalizeAngle(0.0), 0.0)
        assertEquals(180.0, SolMath.normalizeAngle(180.0), 0.0)
        assertEquals(0.0, SolMath.normalizeAngle(0.0), 0.0)
        assertEquals(1.0, SolMath.normalizeAngle(361.0), 0.0)
        assertEquals(359.0, SolMath.normalizeAngle(-1.0), 0.0)
        assertEquals(180.0, SolMath.normalizeAngle(-180.0), 0.0)
        assertEquals(0.0, SolMath.normalizeAngle(720.0), 0.0)
    }

    @ParameterizedTest
    @MethodSource("provideDeltaAngle")
    fun square(angle1: Float, angle2: Float, expected: Float) {
        val actual = SolMath.deltaAngle(angle1, angle2)
        assertEquals(expected, actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideTanDegrees")
    fun tanDegreesDouble(angle: Double, expected: Double) {
        val actual = SolMath.tanDegrees(angle)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideTanDegrees")
    fun tanDegreesFloat(angle: Double, expected: Double) {
        val actual = SolMath.tanDegrees(angle.toFloat())
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideSinDegrees")
    fun sinDegreesDouble(angle: Double, expected: Double) {
        val actual = SolMath.sinDegrees(angle)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideSinDegrees")
    fun sinDegreesFloat(angle: Double, expected: Double) {
        val actual = SolMath.sinDegrees(angle.toFloat())
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideCosDegrees")
    fun cosDegreesDouble(angle: Double, expected: Double) {
        val actual = SolMath.cosDegrees(angle)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideCosDegrees")
    fun cosDegreesFloat(angle: Double, expected: Double) {
        val actual = SolMath.cosDegrees(angle.toFloat())
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

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
    @MethodSource("provideClamp")
    fun clampDouble(value: Double, min: Double, max: Double, expected: Double) {
        val actual = SolMath.clamp(value, min, max)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideClamp")
    fun clampFloat(value: Double, min: Double, max: Double, expected: Double) {
        val actual = SolMath.clamp(value.toFloat(), min.toFloat(), max.toFloat())
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideNorm")
    fun normDouble(value: Double, min: Double, max: Double, expected: Double) {
        val actual = SolMath.norm(value, min, max)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideNorm")
    fun normFloat(value: Double, min: Double, max: Double, expected: Double) {
        val actual = SolMath.norm(value.toFloat(), min.toFloat(), max.toFloat())
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideLerp")
    fun lerpDouble(value: Double, min: Double, max: Double, expected: Double) {
        val actual = SolMath.lerp(value, min, max)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideLerp")
    fun lerpFloat(value: Double, min: Double, max: Double, expected: Double) {
        val actual = SolMath.lerp(value.toFloat(), min.toFloat(), max.toFloat())
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideMap")
    fun mapDouble(
        value: Double,
        min: Double,
        max: Double,
        newMin: Double,
        newMax: Double,
        expected: Double
    ) {
        val actual = SolMath.map(value, min, max, newMin, newMax)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideMap")
    fun mapFloat(
        value: Double,
        min: Double,
        max: Double,
        newMin: Double,
        newMax: Double,
        expected: Double
    ) {
        val actual = SolMath.map(
            value.toFloat(),
            min.toFloat(),
            max.toFloat(),
            newMin.toFloat(),
            newMax.toFloat()
        )
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideScaleToFit")
    fun scaleToFit(
        width: Float,
        height: Float,
        maxWidth: Float,
        maxHeight: Float,
        expected: Float
    ) {
        val actual = SolMath.scaleToFit(width, height, maxWidth, maxHeight)
        assertEquals(expected, actual, 0.00001f)
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
    @MethodSource("provideArgmax")
    fun <T: Comparable<T>> argmax(value: List<T>, expected: Int){
        val actual = SolMath.argmax(value)
        assertEquals(expected, actual)
    }

    companion object {

        @JvmStatic
        fun provideArgmax(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(listOf(1f, 2f, 3f), 2),
                Arguments.of(listOf(1f, 0.5f, 0.75f), 0),
                Arguments.of(listOf(1f, 1f, 1f), 0),
                Arguments.of(listOf<Float>(), -1),
            )
        }

        @JvmStatic
        fun provideWrapDouble(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0.2, 0.0, 1.0, 0.2),
                Arguments.of(1.0, 0.0, 1.0, 1.0),
                Arguments.of(0.0, 0.0, 1.0, 0.0),
                Arguments.of(1.5, 0.0, 1.0, 0.5),
                Arguments.of(-0.75, 0.0, 1.0, 0.25),
                Arguments.of(0.0, 1.0, 4.0, 3.0),
                Arguments.of(5.0, 1.0, 4.0, 2.0),
                Arguments.of(6.0, 5.0, 4.0, 6.0),
            )
        }

        @JvmStatic
        fun provideWrapFloat(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0.2f, 0.0f, 1.0f, 0.2f),
                Arguments.of(1.0f, 0.0f, 1.0f, 1.0f),
                Arguments.of(0.0f, 0.0f, 1.0f, 0.0f),
                Arguments.of(1.5f, 0.0f, 1.0f, 0.5f),
                Arguments.of(-0.75f, 0.0f, 1.0f, 0.25f),
                Arguments.of(0.0f, 1.0f, 4.0f, 3.0f),
                Arguments.of(5.0f, 1.0f, 4.0f, 2.0f),
                Arguments.of(6.0f, 5.0f, 4.0f, 6.0f),
            )
        }

        @JvmStatic
        fun providePower(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1.0, 2, 1.0),
                Arguments.of(1.0, 0, 1.0),
                Arguments.of(1.0, -1, 1.0),
                Arguments.of(3.0, -1, 1 / 3.0),
                Arguments.of(3.0, -2, 1 / 9.0),
                Arguments.of(3.0, 0, 1.0),
                Arguments.of(3.0, 1, 3.0),
                Arguments.of(3.0, 2, 9.0),
                Arguments.of(0.0, 2, 0.0),
                Arguments.of(-2.0, 2, 4.0),
                Arguments.of(-2.0, 3, -8.0),
                Arguments.of(0.5, 2, 0.25),
                Arguments.of(0.5, -2, 4.0),
            )
        }

        @JvmStatic
        fun providePowerInt(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1, 2, 1),
                Arguments.of(1, 0, 1),
                Arguments.of(1, -1, 1),
                Arguments.of(3, -1, 0),
                Arguments.of(3, -2, 0),
                Arguments.of(3, 0, 1),
                Arguments.of(3, 1, 3),
                Arguments.of(3, 2, 9),
                Arguments.of(-2, 2, 4),
                Arguments.of(-2, 3, -8),
                Arguments.of(0, 3, 0)
            )
        }

        @JvmStatic
        fun providePolynomial(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1.0, doubleArrayOf(1.0, 2.0, 3.0), 6.0),
                Arguments.of(2.0, doubleArrayOf(1.0, 2.0, 3.0), 17.0),
                Arguments.of(3.0, doubleArrayOf(0.0, 1.0, 3.0, 1.0), 57.0),
                Arguments.of(3.0, doubleArrayOf(0.0, 1.0, -3.0, 1.0), 3.0),
                Arguments.of(3.0, doubleArrayOf(), 0.0),
                Arguments.of(3.0, doubleArrayOf(1.0), 1.0),
            )
        }

        @JvmStatic
        fun provideCube(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1.0, 1.0),
                Arguments.of(2.0, 8.0),
                Arguments.of(3.0, 27.0),
                Arguments.of(4.0, 64.0),
                Arguments.of(-4.0, -64.0),
                Arguments.of(0.0, 0.0),
                Arguments.of(0.5, 0.125),
            )
        }

        @JvmStatic
        fun provideSquare(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1.0, 1.0),
                Arguments.of(2.0, 4.0),
                Arguments.of(3.0, 9.0),
                Arguments.of(4.0, 16.0),
                Arguments.of(-4.0, 16.0),
                Arguments.of(0.0, 0.0),
                Arguments.of(0.5, 0.25),
            )
        }

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
        fun provideClamp(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0.1, 0.0, 1.0, 0.1),
                Arguments.of(0.0, 0.0, 1.0, 0.0),
                Arguments.of(1.0, 0.0, 1.0, 1.0),
                Arguments.of(1.2, 0.0, 1.0, 1.0),
                Arguments.of(-0.1, 0.0, 1.0, 0.0),
                Arguments.of(4.0, 2.0, 5.0, 4.0),
                Arguments.of(1.0, 2.0, 5.0, 2.0),
                Arguments.of(6.0, 2.0, 5.0, 5.0),
            )
        }

        @JvmStatic
        fun provideNorm(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0.1, 0.0, 1.0, 0.1),
                Arguments.of(0.0, 0.0, 1.0, 0.0),
                Arguments.of(1.0, 0.0, 1.0, 1.0),
                Arguments.of(1.2, 0.0, 1.0, 1.2),
                Arguments.of(-0.1, 0.0, 1.0, -0.1),
                Arguments.of(4.0, 2.0, 6.0, 0.5),
                Arguments.of(1.0, 2.0, 6.0, -0.25),
                Arguments.of(6.0, 2.0, 6.0, 1.0),
                Arguments.of(2.0, 2.0, 6.0, 0.0),
            )
        }

        @JvmStatic
        fun provideLerp(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0.1, 0.0, 1.0, 0.1),
                Arguments.of(0.0, 0.0, 1.0, 0.0),
                Arguments.of(1.0, 0.0, 1.0, 1.0),
                Arguments.of(1.2, 0.0, 1.0, 1.2),
                Arguments.of(-0.1, 0.0, 1.0, -0.1),
                Arguments.of(0.5, 2.0, 6.0, 4.0),
                Arguments.of(-0.25, 2.0, 6.0, 1.0),
                Arguments.of(1.0, 2.0, 6.0, 6.0),
                Arguments.of(0.0, 2.0, 6.0, 2.0),
            )
        }

        @JvmStatic
        fun provideMap(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0.1, 0.0, 1.0, 2.0, 4.0, 2.2),
                Arguments.of(0.0, 0.0, 1.0, 2.0, 4.0, 2.0),
                Arguments.of(1.0, 0.0, 1.0, 2.0, 4.0, 4.0),
                Arguments.of(1.2, 0.0, 1.0, 2.0, 4.0, 4.4),
                Arguments.of(-0.1, 0.0, 1.0, 2.0, 4.0, 1.8),
                Arguments.of(4.0, 2.0, 6.0, 0.0, 4.0, 2.0),
                Arguments.of(1.0, 2.0, 6.0, 0.0, 4.0, -1.0),
                Arguments.of(6.0, 2.0, 6.0, 0.0, 4.0, 4.0),
                Arguments.of(2.0, 2.0, 6.0, 0.0, 4.0, 0.0),
            )
        }

        @JvmStatic
        fun provideScaleToFit(): Stream<Arguments> {
            return Stream.of(
                // Square destination
                Arguments.of(2.0f, 2.0f, 2.0f, 2.0f, 1.0f),
                Arguments.of(1.0f, 1.0f, 2.0f, 2.0f, 2.0f),
                Arguments.of(4.0f, 4.0f, 2.0f, 2.0f, 0.5f),
                Arguments.of(4.0f, 2.0f, 2.0f, 2.0f, 0.5f),
                Arguments.of(2.0f, 4.0f, 2.0f, 2.0f, 0.5f),
                Arguments.of(1.0f, 4.0f, 2.0f, 2.0f, 0.5f),
                Arguments.of(4.0f, 1.0f, 2.0f, 2.0f, 0.5f),
                Arguments.of(2.0f, 1.0f, 2.0f, 2.0f, 1.0f),
                Arguments.of(1.0f, 2.0f, 2.0f, 2.0f, 1.0f),
                Arguments.of(1.0f, 0.5f, 2.0f, 2.0f, 2.0f),
                Arguments.of(0.5f, 1.0f, 2.0f, 2.0f, 2.0f),

                // Long destination
                Arguments.of(2.0f, 2.0f, 4.0f, 2.0f, 1.0f),
                Arguments.of(1.0f, 1.0f, 4.0f, 2.0f, 2.0f),
                Arguments.of(4.0f, 4.0f, 4.0f, 2.0f, 0.5f),
                Arguments.of(4.0f, 2.0f, 4.0f, 2.0f, 1.0f),
                Arguments.of(2.0f, 4.0f, 4.0f, 2.0f, 0.5f),
                Arguments.of(1.0f, 4.0f, 4.0f, 2.0f, 0.5f),
                Arguments.of(4.0f, 1.0f, 4.0f, 2.0f, 1.0f),
                Arguments.of(2.0f, 1.0f, 4.0f, 2.0f, 2.0f),
                Arguments.of(1.0f, 2.0f, 4.0f, 2.0f, 1.0f),
                Arguments.of(1.0f, 0.5f, 4.0f, 2.0f, 4.0f),
                Arguments.of(0.5f, 1.0f, 4.0f, 2.0f, 2.0f),

                // Tall destination
                Arguments.of(2.0f, 2.0f, 2.0f, 4.0f, 1.0f),
                Arguments.of(1.0f, 1.0f, 2.0f, 4.0f, 2.0f),
                Arguments.of(4.0f, 4.0f, 2.0f, 4.0f, 0.5f),
                Arguments.of(4.0f, 2.0f, 2.0f, 4.0f, 0.5f),
                Arguments.of(2.0f, 4.0f, 2.0f, 4.0f, 1.0f),
                Arguments.of(1.0f, 4.0f, 2.0f, 4.0f, 1.0f),
                Arguments.of(4.0f, 1.0f, 2.0f, 4.0f, 0.5f),
                Arguments.of(2.0f, 1.0f, 2.0f, 4.0f, 1.0f),
                Arguments.of(1.0f, 2.0f, 2.0f, 4.0f, 2.0f),
                Arguments.of(1.0f, 0.5f, 2.0f, 4.0f, 2.0f),
                Arguments.of(0.5f, 1.0f, 2.0f, 4.0f, 4.0f),
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