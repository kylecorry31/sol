package com.kylecorry.sol.science.geophysics

import assertk.assertThat
import assertk.assertions.isCloseTo
import com.kylecorry.sol.time.Time.toUTC
import com.kylecorry.sol.units.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate

internal class GeophysicsTest {

    @Test
    fun gravity() {
        assertThat(Geophysics.getGravity(Coordinate.zero)).isCloseTo(9.78032677f, 0.00001f)
        assertThat(Geophysics.getGravity(Coordinate(90.0, 0.0))).isCloseTo(
            9.83218493786340046183f,
            0.00001f
        )
        assertThat(Geophysics.getGravity(Coordinate(-90.0, 0.0))).isCloseTo(
            9.83218493786340046183f,
            0.00001f
        )
    }

    @ParameterizedTest
    @CsvSource(
        "2025-01-01, 80.0, 0.0, 0.0, 1.28",
        "2025-01-01, 0.0, 120.0, 0.0, -0.16",
        "2025-01-01, -80.0, 240.0, 0.0, 68.78",
        "2025-01-01, 80.0, 0.0, 100000.0, 0.85",
        "2025-01-01, 0.0, 120.0, 100000.0, -0.15",
        "2025-01-01, -80.0, 240.0, 100000.0, 68.21",
        "2027-07-01, 80.0, 0.0, 0.0, 2.59",
        "2027-07-01, 0.0, 120.0, 0.0, -0.24",
        "2027-07-01, -80.0, 240.0, 0.0, 68.49",
        "2027-07-01, 80.0, 0.0, 100000.0, 2.16",
        "2027-07-01, 0.0, 120.0, 100000.0, -0.23",
        "2027-07-01, -80.0, 240.0, 100000.0, 67.93"
    )
    fun getGeomagneticDeclination(dateStr: String, lat: Double, lon: Double, altitude: Float, expected: Float) {
        val location = Coordinate(lat, lon)
        val actual = Geophysics.getGeomagneticDeclination(
            location,
            altitude,
            LocalDate.parse(dateStr).atStartOfDay().toUTC().toInstant().toEpochMilli()
        )
        assertEquals(expected, actual, 0.01f)
    }

    @ParameterizedTest
    @CsvSource(
        "2025-01-01, 80.0, 0.0, 0.0, 83.21",
        "2025-01-01, 0.0, 120.0, 0.0, -14.93",
        "2025-01-01, -80.0, 240.0, 0.0, -72.0",
        "2025-01-01, 80.0, 0.0, 100000.0, 83.26",
        "2025-01-01, 0.0, 120.0, 100000.0, -15.08",
        "2025-01-01, -80.0, 240.0, 100000.0, -72.19",
        "2027-07-01, 80.0, 0.0, 0.0, 83.24",
        "2027-07-01, 0.0, 120.0, 0.0, -14.65",
        "2027-07-01, -80.0, 240.0, 0.0, -71.92",
        "2027-07-01, 80.0, 0.0, 100000.0, 83.29",
        "2027-07-01, 0.0, 120.0, 100000.0, -14.81",
        "2027-07-01, -80.0, 240.0, 100000.0, -72.1"
    )
    fun getGeomagneticInclination(dateStr: String, lat: Double, lon: Double, altitude: Float, expected: Float) {
        val location = Coordinate(lat, lon)
        val actual = Geophysics.getGeomagneticInclination(
            location,
            altitude,
            LocalDate.parse(dateStr).atStartOfDay().toUTC().toInstant().toEpochMilli()
        )
        assertEquals(expected, actual, 0.01f)
    }

}
