package com.kylecorry.sol.math.geometry

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class SizeTest {

    @ParameterizedTest
    @CsvSource(
        // Right angles
        "100, 200, 0, 100, 200",
        "100, 200, 90, 200, 100",
        "100, 200, 180, 100, 200",
        "100, 200, 270, 200, 100",
        // 45 degrees
        "100, 200, 45, 212.1320317678, 212.1320317678",
        "100, 200, 135, 212.1320317678, 212.1320317678",
        "100, 200, 225, 212.1320317678, 212.1320317678",
        "100, 200, 315, 212.1320317678, 212.1320317678",
        // 30 degrees
        "100, 200, 30, 186.6025433849, 223.20508043",
        "100, 200, 60, 223.20508043, 186.6025433849",
        "100, 200, 120, 223.20508043, 186.6025433849",
        "100, 200, 150, 186.6025433849, 223.20508043",
        "100, 200, 210, 186.6025433849, 223.20508043",
        "100, 200, 240, 223.20508043, 186.6025433849",
        "100, 200, 300, 223.20508043, 186.6025433849",
        "100, 200, 330, 186.6025433849, 223.20508043",
    )
    fun getSize(
        width: Float,
        height: Float,
        rotation: Float,
        expectedWidth: Float,
        expectedHeight: Float
    ) {
        val size = Size(width, height).rotate(rotation)
        assertEquals(expectedWidth, size.width, 0.0001f)
        assertEquals(expectedHeight, size.height, 0.0001f)
    }
    
}