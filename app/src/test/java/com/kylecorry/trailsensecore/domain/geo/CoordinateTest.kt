package com.kylecorry.trailsensecore.domain.geo

import org.junit.Assert
import org.junit.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class CoordinateTest {

    @Test
    fun toUTM() {
        Assert.assertEquals("19T 0282888E 4674752N", Coordinate(42.1948, -71.6295).toUTM())
        Assert.assertEquals("14T 0328056E 5290773N", Coordinate(47.7474, -101.2939).toUTM())
        Assert.assertEquals("13R 0393008E 3051634N", Coordinate(27.5844, -106.0840).toUTM())
        Assert.assertEquals("21L 0359923E 9098523N", Coordinate(-8.1534, -58.2715).toUTM())
        Assert.assertEquals("34H 0674432E 6430470N", Coordinate(-32.2489, 22.8516).toUTM())
        Assert.assertEquals("34H 0674432E 6430470N", Coordinate(-32.2489, 22.8516).toUTM())
        Assert.assertEquals("34D 0528288E 2071725N", Coordinate(-71.4545, 21.7969).toUTM())
        Assert.assertEquals("40X 0545559E 9051365N", Coordinate(81.5113, 59.7656).toUTM())
        Assert.assertEquals("17M 0784692E 9999203N", Coordinate(-0.0072, -78.4424).toUTM())
        Assert.assertEquals("09E 0353004E 3573063N", Coordinate(-57.9598, -131.4844).toUTM())

        // Different precisions
        Assert.assertEquals("19T 0282888E 4674752N", Coordinate(42.1948, -71.6295).toUTM(7))
        Assert.assertEquals("19T 0282880E 4674750N", Coordinate(42.1948, -71.6295).toUTM(6))
        Assert.assertEquals("19T 0282800E 4674700N", Coordinate(42.1948, -71.6295).toUTM(5))
        Assert.assertEquals("19T 0282000E 4674000N", Coordinate(42.1948, -71.6295).toUTM(4))
        Assert.assertEquals("19T 0280000E 4670000N", Coordinate(42.1948, -71.6295).toUTM(3))
        Assert.assertEquals("19T 0200000E 4600000N", Coordinate(42.1948, -71.6295).toUTM(2))
        Assert.assertEquals("19T 0000000E 4000000N", Coordinate(42.1948, -71.6295).toUTM(1))
    }

    @Test
    fun canAddDistance() {
        val start = Coordinate(40.0, 10.0)
        val bearing = Bearing(100f)
        val distance = 10000.0

        val expected = Coordinate(39.984444, 10.115556)
        val actual = start.plus(distance, bearing)
        Assert.assertEquals(expected.latitude, actual.latitude, 0.01)
        Assert.assertEquals(expected.longitude, actual.longitude, 0.01)
    }

    @ParameterizedTest
    @MethodSource("provideDDM")
    fun toDDM(expected: String, coordinate: Coordinate, precision: Int) {
        Assert.assertEquals(expected, coordinate.toDegreeDecimalMinutes(precision))
    }

    @ParameterizedTest
    @MethodSource("provideDMS")
    fun toDMS(expected: String, coordinate: Coordinate, precision: Int) {
        Assert.assertEquals(expected, coordinate.toDegreeMinutesSeconds(precision))
    }

    @ParameterizedTest
    @MethodSource("provideDD")
    fun toDD(expected: String, coordinate: Coordinate, precision: Int) {
        Assert.assertEquals(expected, coordinate.toDecimalDegrees(precision))
    }

    @ParameterizedTest
    @MethodSource("provideLocationStrings")
    fun parse(locationString: String, expected: Coordinate?) {
        assertCoordinatesEqual(Coordinate.parse(locationString), expected, 0.0001)
    }

    private fun assertCoordinatesEqual(
        actual: Coordinate?,
        expected: Coordinate?,
        precision: Double
    ) {
        if (expected == null) {
            Assert.assertNull(actual)
            return
        }
        Assert.assertNotNull(actual)
        Assert.assertEquals(expected.latitude, actual!!.latitude, precision)
        Assert.assertEquals(expected.longitude, actual.longitude, precision)
    }

    companion object {

        @JvmStatic
        fun provideDDM(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("10°2.1'N    77°30.5'E", Coordinate(10.03472, 77.508333), 1),
                Arguments.of("10°2.1'S    77°30.5'E", Coordinate(-10.03472, 77.508333), 1),
                Arguments.of("10°2.08'N    77°30.5'W", Coordinate(10.03472, -77.508333), 2),
                Arguments.of("10°2.0832'S    77°30.5'W", Coordinate(-10.03472, -77.508333), 4),
            )
        }

        @JvmStatic
        fun provideDMS(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("10°2'5.0\"N    77°30'30.0\"E", Coordinate(10.03472, 77.508333), 1),
                Arguments.of("10°2'5.0\"S    77°30'30.0\"E", Coordinate(-10.03472, 77.508333), 1),
                Arguments.of("10°2'4.99\"N    77°30'30.0\"W", Coordinate(10.03472, -77.508333), 2),
                Arguments.of("10°7'23.16\"S    77°7'23.232\"W", Coordinate(-10.1231, -77.12312), 3),
            )
        }

        @JvmStatic
        fun provideDD(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("42.1948°,  -71.6295°", Coordinate(42.1948, -71.6295), 4),
                Arguments.of("-90.0°,  -180.0°", Coordinate(-90.0, -180.0), 0),
                Arguments.of("-42.19°,  -71.63°", Coordinate(-42.1948, -71.6295), 2),
                Arguments.of("1.2°,  1.4°", Coordinate(1.2, 1.4), 1),
            )
        }

        @JvmStatic
        fun provideLocationStrings(): Stream<Arguments> {
            return Stream.of(
                // DDM
                Arguments.of("10°2.083333' N, 77°30.5' E", Coordinate(10.03472, 77.508333)),
                Arguments.of("10°2.083333' S, 77°30.5' E", Coordinate(-10.03472, 77.508333)),
                Arguments.of("10°2.083333' N, 77°30.5' W", Coordinate(10.03472, -77.508333)),
                Arguments.of("10°2.083333' S, 77°30.5' W", Coordinate(-10.03472, -77.508333)),
                // DMS
                Arguments.of("10°2'5.0\" N, 77°30'30.0\" E", Coordinate(10.03472, 77.508333)),
                Arguments.of("10°2'5.0\" S, 77°30'30.0\" E", Coordinate(-10.03472, 77.508333)),
                Arguments.of("10°2'5.0\" N, 77°30'30.0\" W", Coordinate(10.03472, -77.508333)),
                Arguments.of("10°2'5.0\" S, 77°30'30.0\" W", Coordinate(-10.03472, -77.508333)),
                Arguments.of("10°2'5\" S, 77°30'30\" W", Coordinate(-10.03472, -77.508333)),
                // DD
                Arguments.of("42.1948, -71.6295", Coordinate(42.1948, -71.6295)),
                Arguments.of("-90, -180", Coordinate(-90.0, -180.0)),
                Arguments.of("90, 180", Coordinate(90.0, 180.0)),
                Arguments.of("-42.1948, -71.6295", Coordinate(-42.1948, -71.6295)),
                Arguments.of("1.2,1.4", Coordinate(1.2, 1.4)),
                Arguments.of("1.2°, 1.4°", Coordinate(1.2, 1.4)),
                Arguments.of("1 8", Coordinate(1.0, 8.0)),
                // UTM
                Arguments.of("19T 0282888E 4674752N", Coordinate(42.1948, -71.6295)),
                Arguments.of("14T 0328056E 5290773N", Coordinate(47.7474, -101.2939)),
                Arguments.of("13R 0393008E 3051634N", Coordinate(27.5844, -106.0840)),
                Arguments.of("21L 0359923E 9098523N", Coordinate(-8.1534, -58.2715)),
                Arguments.of("34H 0674432E 6430470N", Coordinate(-32.2489, 22.8516)),
                Arguments.of("34H 0674432E 6430470N", Coordinate(-32.2489, 22.8516)),
                Arguments.of("34D 0528288E 2071725N", Coordinate(-71.4545, 21.7969)),
                Arguments.of("40X 0545559E 9051365N", Coordinate(81.5113, 59.7656)),
                Arguments.of("17M 0784692E 9999203N", Coordinate(-0.0072, -78.4424)),
                Arguments.of("09E 0353004E 3573063N", Coordinate(-57.9598, -131.4844)),
                Arguments.of("09e 0353004e 3573063n", Coordinate(-57.9598, -131.4844)),
                Arguments.of("09 E 0353004 E 3573063 N", Coordinate(-57.9598, -131.4844)),
                Arguments.of("09E 0353004 E 3573063 N", Coordinate(-57.9598, -131.4844)),

                // Invalid formats / locations
                Arguments.of("91 8", null),
                Arguments.of("-91 8", null),
                Arguments.of("1 181", null),
                Arguments.of("1 -181", null),
                Arguments.of("test", null),
                Arguments.of("1 1231E 1231N", null),
                Arguments.of("1a 1231E 1231N", null),
                Arguments.of("1b 1231E 1231N", null),
                Arguments.of("1y 1231E 1231N", null),
                Arguments.of("1z 1231E 1231N", null),
                Arguments.of("12m1, 4m1", null),
                Arguments.of("61T 1234E 1234N", null),
                Arguments.of("1", null),
                Arguments.of("", null),
                Arguments.of("10°2'5.0\", 77°30'30.0\"", null),
                Arguments.of("10°2'5.0\" W, 77°30'30.0\" N", null),
                Arguments.of("10°2'5.0 S, 77°30'30.0\" W", null),
                Arguments.of("10°2'5.0 S\", 30'30.0\" W", null),
                Arguments.of("10°2' S\", 77°30'30.0\" W", null),
                Arguments.of("10°5\" S, 77°30'30.0\" W", null)

                )
        }
    }

}