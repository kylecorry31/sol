package com.kylecorry.trailsensecore.domain.geo

import org.junit.Assert.*
import org.junit.Test

class CoordinateTest {

    @Test
    fun canAddDistance(){
        val start = Coordinate(40.0, 10.0)
        val bearing = Bearing(100f)
        val distance = 10000.0

        val expected = Coordinate(39.984444, 10.115556)
        val actual = start.plus(distance, bearing)
        assertEquals(expected.latitude, actual.latitude, 0.01)
        assertEquals(expected.longitude, actual.longitude, 0.01)
    }

    @Test
    fun canConvertToString(){
        assertEquals("10°2'5.0\" N    77°30'30.0\" E", Coordinate(10.03472, 77.508333).toString())
        assertEquals("10°2'5.0\" S    77°30'30.0\" E", Coordinate(-10.03472, 77.508333).toString())
        assertEquals("10°2'5.0\" N    77°30'30.0\" W", Coordinate(10.03472, -77.508333).toString())
        assertEquals("10°2'5.0\" S    77°30'30.0\" W", Coordinate(-10.03472, -77.508333).toString())
    }

    @Test
    fun canParseLocationString(){
        assertCoordinatesEqual(Coordinate.parse("19T 0282888E 4674752N"), Coordinate(42.1948, -71.6295), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("42.1948, -71.6295"), Coordinate(42.1948, -71.6295), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("10°2'5.0\" N, 77°30'30.0\" E"), Coordinate(10.03472, 77.508333), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("10°2.083333' N, 77°30.5' E"), Coordinate(10.03472, 77.508333), 0.0001)
    }

    @Test
    fun canConvertToUTM(){
        assertEquals("19T 0282888E 4674752N", Coordinate(42.1948, -71.6295).toUTM())
        assertEquals("14T 0328056E 5290773N", Coordinate(47.7474, -101.2939).toUTM())
        assertEquals("13R 0393008E 3051634N", Coordinate(27.5844, -106.0840).toUTM())
        assertEquals("21L 0359923E 9098523N", Coordinate(-8.1534, -58.2715).toUTM())
        assertEquals("34H 0674432E 6430470N", Coordinate(-32.2489, 22.8516).toUTM())
        assertEquals("34H 0674432E 6430470N", Coordinate(-32.2489, 22.8516).toUTM())
        assertEquals("34D 0528288E 2071725N", Coordinate(-71.4545, 21.7969).toUTM())
        assertEquals("40X 0545559E 9051365N", Coordinate(81.5113, 59.7656).toUTM())
        assertEquals("17M 0784692E 9999203N", Coordinate(-0.0072, -78.4424).toUTM())
        assertEquals("09E 0353004E 3573063N", Coordinate(-57.9598, -131.4844).toUTM())

        // Different precisions
        assertEquals("19T 0282888E 4674752N", Coordinate(42.1948, -71.6295).toUTM(7))
        assertEquals("19T 0282880E 4674750N", Coordinate(42.1948, -71.6295).toUTM(6))
        assertEquals("19T 0282800E 4674700N", Coordinate(42.1948, -71.6295).toUTM(5))
        assertEquals("19T 0282000E 4674000N", Coordinate(42.1948, -71.6295).toUTM(4))
        assertEquals("19T 0280000E 4670000N", Coordinate(42.1948, -71.6295).toUTM(3))
        assertEquals("19T 0200000E 4600000N", Coordinate(42.1948, -71.6295).toUTM(2))
        assertEquals("19T 0000000E 4000000N", Coordinate(42.1948, -71.6295).toUTM(1))
    }

    @Test
    fun canConvertFromUTM(){
        assertCoordinatesEqual(Coordinate.parse("19T 0282888E 4674752N", CoordinateFormat.UTM), Coordinate(42.1948, -71.6295), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("14T 0328056E 5290773N", CoordinateFormat.UTM), Coordinate(47.7474, -101.2939), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("13R 0393008E 3051634N", CoordinateFormat.UTM), Coordinate(27.5844, -106.0840), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("21L 0359923E 9098523N", CoordinateFormat.UTM), Coordinate(-8.1534, -58.2715), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("34H 0674432E 6430470N", CoordinateFormat.UTM), Coordinate(-32.2489, 22.8516), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("34H 0674432E 6430470N", CoordinateFormat.UTM), Coordinate(-32.2489, 22.8516), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("34D 0528288E 2071725N", CoordinateFormat.UTM), Coordinate(-71.4545, 21.7969), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("40X 0545559E 9051365N", CoordinateFormat.UTM), Coordinate(81.5113, 59.7656), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("17M 0784692E 9999203N", CoordinateFormat.UTM), Coordinate(-0.0072, -78.4424), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("09E 0353004E 3573063N", CoordinateFormat.UTM), Coordinate(-57.9598, -131.4844), 0.0001)

        // Different formatting
        assertCoordinatesEqual(Coordinate.parse("09e 0353004e 3573063n", CoordinateFormat.UTM), Coordinate(-57.9598, -131.4844), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("09 E 0353004 E 3573063 N", CoordinateFormat.UTM), Coordinate(-57.9598, -131.4844), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("09E 0353004 E 3573063 N", CoordinateFormat.UTM), Coordinate(-57.9598, -131.4844), 0.0001)
    }

    @Test
    fun canConvertFromDD(){
        assertCoordinatesEqual(Coordinate.parse("42.1948, -71.6295", CoordinateFormat.DecimalDegrees), Coordinate(42.1948, -71.6295), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("-42.1948, -71.6295", CoordinateFormat.DecimalDegrees), Coordinate(-42.1948, -71.6295), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("1.2,1.4", CoordinateFormat.DecimalDegrees), Coordinate(1.2, 1.4), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("1.2°, 1.4°", CoordinateFormat.DecimalDegrees), Coordinate(1.2, 1.4), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("1 8", CoordinateFormat.DecimalDegrees), Coordinate(1.0, 8.0), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("91 8", CoordinateFormat.DecimalDegrees), null, 0.0001)
        assertCoordinatesEqual(Coordinate.parse("-91 8", CoordinateFormat.DecimalDegrees), null, 0.0001)
        assertCoordinatesEqual(Coordinate.parse("1 181", CoordinateFormat.DecimalDegrees), null, 0.0001)
        assertCoordinatesEqual(Coordinate.parse("1 -181", CoordinateFormat.DecimalDegrees), null, 0.0001)
    }

    @Test
    fun canConvertFromDMS(){
        assertCoordinatesEqual(Coordinate.parse("10°2'5.0\" N, 77°30'30.0\" E", CoordinateFormat.DegreesMinutesSeconds), Coordinate(10.03472, 77.508333), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("10°2'5.0\" S, 77°30'30.0\" E", CoordinateFormat.DegreesMinutesSeconds), Coordinate(-10.03472, 77.508333), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("10°2'5.0\" N, 77°30'30.0\" W", CoordinateFormat.DegreesMinutesSeconds), Coordinate(10.03472, -77.508333), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("10°2'5.0\" S, 77°30'30.0\" W", CoordinateFormat.DegreesMinutesSeconds), Coordinate(-10.03472, -77.508333), 0.0001)
    }

    @Test
    fun canConvertFromDDM(){
        assertCoordinatesEqual(Coordinate.parse("10°2.083333' N, 77°30.5' E", CoordinateFormat.DegreesDecimalMinutes), Coordinate(10.03472, 77.508333), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("10°2.083333' S, 77°30.5' E", CoordinateFormat.DegreesDecimalMinutes), Coordinate(-10.03472, 77.508333), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("10°2.083333' N, 77°30.5' W", CoordinateFormat.DegreesDecimalMinutes), Coordinate(10.03472, -77.508333), 0.0001)
        assertCoordinatesEqual(Coordinate.parse("10°2.083333' S, 77°30.5' W", CoordinateFormat.DegreesDecimalMinutes), Coordinate(-10.03472, -77.508333), 0.0001)
    }

    private fun assertCoordinatesEqual(actual: Coordinate?, expected: Coordinate?, precision: Double){
        if (expected == null){
            assertNull(actual)
            return
        }
        assertNotNull(actual)
        assertEquals(expected.latitude, actual!!.latitude, precision)
        assertEquals(expected.longitude, actual.longitude, precision)
    }

}