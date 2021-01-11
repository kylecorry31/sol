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
        assertEquals("31 N 166021 0", Coordinate(0.0, 0.0).toUTM())
        assertEquals("30 N 808084 14386", Coordinate(0.1300, -0.2324).toUTM())
        assertEquals("34 S 683474 4942631", Coordinate(-45.6456, 23.3545).toUTM())
        assertEquals("25 S 404859 8588691", Coordinate(-12.7650, -33.8765).toUTM())
        assertEquals("02 S 506346 1057743", Coordinate(-80.5434, -170.6540).toUTM())
//        assertEquals("60 N 500000 9997964", Coordinate(90.0000, 177.0000).toUTM())
//        assertEquals("01 S 500000 2035", Coordinate(-90.0000, -177.0000).toUTM())
//        assertEquals("31 N 500000 9997964", Coordinate(90.0000, 3.0000).toUTM())
        assertEquals("08 N 453580 2594273", Coordinate(23.4578, -135.4545).toUTM())
        assertEquals("57 N 450794 8586116", Coordinate(77.3450, 156.9876).toUTM())
//        assertEquals("22 S 502639 75072", Coordinate(-89.3454, -48.9306).toUTM())
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

}