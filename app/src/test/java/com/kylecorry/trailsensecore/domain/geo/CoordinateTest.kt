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
        assertEquals("10°2'5.0\" N, 77°30'30.0\" E", Coordinate(10.03472, 77.508333).toString())
        assertEquals("10°2'5.0\" S, 77°30'30.0\" E", Coordinate(-10.03472, 77.508333).toString())
        assertEquals("10°2'5.0\" N, 77°30'30.0\" W", Coordinate(10.03472, -77.508333).toString())
        assertEquals("10°2'5.0\" S, 77°30'30.0\" W", Coordinate(-10.03472, -77.508333).toString())
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
        assertCoordinatesEqual(Coordinate.fromUTM("19T 0282888E 4674752N"), Coordinate(42.1948, -71.6295), 0.0001)
        assertCoordinatesEqual(Coordinate.fromUTM("14T 0328056E 5290773N"), Coordinate(47.7474, -101.2939), 0.0001)
        assertCoordinatesEqual(Coordinate.fromUTM("13R 0393008E 3051634N"), Coordinate(27.5844, -106.0840), 0.0001)
        assertCoordinatesEqual(Coordinate.fromUTM("21L 0359923E 9098523N"), Coordinate(-8.1534, -58.2715), 0.0001)
        assertCoordinatesEqual(Coordinate.fromUTM("34H 0674432E 6430470N"), Coordinate(-32.2489, 22.8516), 0.0001)
        assertCoordinatesEqual(Coordinate.fromUTM("34H 0674432E 6430470N"), Coordinate(-32.2489, 22.8516), 0.0001)
        assertCoordinatesEqual(Coordinate.fromUTM("34D 0528288E 2071725N"), Coordinate(-71.4545, 21.7969), 0.0001)
        assertCoordinatesEqual(Coordinate.fromUTM("40X 0545559E 9051365N"), Coordinate(81.5113, 59.7656), 0.0001)
        assertCoordinatesEqual(Coordinate.fromUTM("17M 0784692E 9999203N"), Coordinate(-0.0072, -78.4424), 0.0001)
        assertCoordinatesEqual(Coordinate.fromUTM("09E 0353004E 3573063N"), Coordinate(-57.9598, -131.4844), 0.0001)

        // Different formatting
        assertCoordinatesEqual(Coordinate.fromUTM("09e 0353004e 3573063n"), Coordinate(-57.9598, -131.4844), 0.0001)
        assertCoordinatesEqual(Coordinate.fromUTM("09 E 0353004 E 3573063 N"), Coordinate(-57.9598, -131.4844), 0.0001)
        assertCoordinatesEqual(Coordinate.fromUTM("09E 0353004 E 3573063 N"), Coordinate(-57.9598, -131.4844), 0.0001)
    }

    @Test
    fun canParseLongitude(){
        val cases = listOf(
            // DMS
            Pair(10.03472, "10°2'5\" E"),
            Pair(-10.03472, "10°2'5\" W"),
//            Pair(10.03472, "10°2'5\""),
//            Pair(-10.03472, "-10°2'5\""),
            Pair(10.03472, "10°2'5\" e"),
            Pair(-10.03472, "10°2'5\" w"),
            Pair(10.03472, "10°2'5\"E"),
            Pair(-10.03472, "10°2'5\"W"),
            Pair(10.03472, "10° 2' 5\" E"),
            Pair(-10.03472, "10° 2' 5\" W"),
            // DDM
            Pair (77.508333, "77°30.5' E"),
            Pair (-77.508333, "77°30.5' W"),
//            Pair (77.508333, "77°30.5'"),
//            Pair (-77.508333, "-77°30.5'"),
            Pair (77.508333, "77°30.5' e"),
            Pair (-77.508333, "77°30.5' w"),
            Pair (77.508333, "77°30.5'E"),
            Pair (-77.508333, "77°30.5'W"),
            Pair (77.508333, "77° 30.5' E"),
            Pair (-77.508333, "77° 30.5' W"),
            // Decimal
//            Pair(12.4, "12.4 E"),
//            Pair(-12.4, "12.4 W"),
            Pair(12.4, "12.4"),
            Pair(-12.4, "-12.4"),
            Pair(180.0, "180"),
            Pair(-180.0, "-180")
        )

        for (case in cases){
            assertEquals(case.first, Coordinate.parseLongitude(case.second)!!, 0.00001)
        }
    }

    @Test
    fun parseReturnsNullWhenInvalidLongitude(){
        val cases = listOf(
            "10°2'5 E",
            "10°2'5\" R",
            "10°25\" E",
            "102'5 E",
            "10°2'5 N",
            "10°2'5 S",
            "a10°2'5 E",
            "",
            "something",
            "181",
            "-181",
            "180°2'5\" E",
            "180°2' E")

        for (case in cases){
            assertNull(Coordinate.parseLongitude(case))
        }
    }

    @Test
    fun canParseLatitude(){
        val cases = listOf(
            // DMS
            Pair(10.03472, "10°2'5\" N"),
            Pair(-10.03472, "10°2'5\" S"),
//            Pair(10.03472, "10°2'5\""),
//            Pair(-10.03472, "-10°2'5\""),
            Pair(10.03472, "10°2'5\" n"),
            Pair(-10.03472, "10°2'5\" s"),
            Pair(10.03472, "10°2'5\"N"),
            Pair(-10.03472, "10°2'5\"S"),
            Pair(10.03472, "10° 2' 5\" N"),
            Pair(-10.03472, "10° 2' 5\" S"),
            // DDM
            Pair (77.508333, "77°30.5' N"),
            Pair (-77.508333, "77°30.5' S"),
//            Pair (77.508333, "77°30.5'"),
//            Pair (-77.508333, "-77°30.5'"),
            Pair (77.508333, "77°30.5' n"),
            Pair (-77.508333, "77°30.5' s"),
            Pair (77.508333, "77°30.5'N"),
            Pair (-77.508333, "77°30.5'S"),
            Pair (77.508333, "77° 30.5' N"),
            Pair (-77.508333, "77° 30.5' S"),
            // Decimal
//            Pair(12.4, "12.4 N"),
//            Pair(-12.4, "12.4 S"),
            Pair(12.4, "12.4"),
            Pair(-12.4, "-12.4"),
            Pair(90.0, "90"),
            Pair(-90.0, "-90")
        )

        for (case in cases){
            assertEquals(case.first, Coordinate.parseLatitude(case.second)!!, 0.00001)
        }
    }

    @Test
    fun parseReturnsNullWhenInvalidLatitude(){
        val cases = listOf(
            "10°2'5 N",
            "10°2'5\" R",
            "10°25\" N",
            "102'5 N",
            "10°2'5 E",
            "10°2'5 W",
            "a10°2'5 S",
            "",
            "something",
            "91",
            "-91",
            "90°2'5\" N",
            "90°2' N")

        for (case in cases){
            assertNull(Coordinate.parseLatitude(case))
        }
    }

    private fun assertCoordinatesEqual(actual: Coordinate?, expected: Coordinate?, precision: Double){
        if (actual == null){
            assertNull(expected)
            return
        }
        assertNotNull(expected)
        assertEquals(actual.latitude, expected!!.latitude, precision)
        assertEquals(actual.longitude, expected!!.longitude, precision)
    }

}