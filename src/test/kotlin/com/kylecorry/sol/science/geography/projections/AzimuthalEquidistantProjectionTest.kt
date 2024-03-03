package com.kylecorry.sol.science.geography.projections

import assertk.assertThat
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.tests.assertVector
import com.kylecorry.sol.tests.isCloseTo
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import org.junit.jupiter.api.Test

class AzimuthalEquidistantProjectionTest {

    @Test
    fun toPixels() {
        val center = Coordinate(0.0, 0.0)
        val projection = AzimuthalEquidistantProjection(center)
        assertVector(Vector2.zero, projection.toPixels(center), 0.001f)
        assertVector(Vector2(0f, 100f), projection.toPixels(center.plus(100.0, Bearing(0f))), 1f)
        assertVector(Vector2(0f, -100f), projection.toPixels(center.plus(100.0, Bearing(180f))), 1f)
        assertVector(Vector2(100f, 0f), projection.toPixels(center.plus(100.0, Bearing(90f))), 1f)
        assertVector(Vector2(-100f, 0f), projection.toPixels(center.plus(100.0, Bearing(270f))), 1f)
    }

    @Test
    fun toPixelsScaled(){
        val center = Coordinate(0.0, 0.0)
        val projection = AzimuthalEquidistantProjection(center, scale = 2f)
        assertVector(Vector2.zero, projection.toPixels(center), 0.001f)
        assertVector(Vector2(0f, 200f), projection.toPixels(center.plus(100.0, Bearing(0f))), 2f)
        assertVector(Vector2(0f, -200f), projection.toPixels(center.plus(100.0, Bearing(180f))), 2f)
        assertVector(Vector2(200f, 0f), projection.toPixels(center.plus(100.0, Bearing(90f))), 2f)
        assertVector(Vector2(-200f, 0f), projection.toPixels(center.plus(100.0, Bearing(270f))), 2f)
    }

    @Test
    fun toPixelsFlipped(){
        val center = Coordinate(0.0, 0.0)
        val projection = AzimuthalEquidistantProjection(center, isYFlipped = true)
        assertVector(Vector2.zero, projection.toPixels(center), 0.001f)
        assertVector(Vector2(0f, -100f), projection.toPixels(center.plus(100.0, Bearing(0f))), 1f)
        assertVector(Vector2(0f, 100f), projection.toPixels(center.plus(100.0, Bearing(180f))), 1f)
        assertVector(Vector2(100f, 0f), projection.toPixels(center.plus(100.0, Bearing(90f))), 1f)
        assertVector(Vector2(-100f, 0f), projection.toPixels(center.plus(100.0, Bearing(270f))), 1f)
    }

    @Test
    fun toPixelsOffset(){
        val center = Coordinate(0.0, 0.0)
        val projection = AzimuthalEquidistantProjection(center, centerPixel = Vector2(10f, 10f))
        assertVector(Vector2(10f, 10f), projection.toPixels(center), 0.001f)
        assertVector(Vector2(10f, 110f), projection.toPixels(center.plus(100.0, Bearing(0f))), 1f)
        assertVector(Vector2(10f, -90f), projection.toPixels(center.plus(100.0, Bearing(180f))), 1f)
        assertVector(Vector2(110f, 10f), projection.toPixels(center.plus(100.0, Bearing(90f))), 1f)
        assertVector(Vector2(-90f, 10f), projection.toPixels(center.plus(100.0, Bearing(270f))), 1f)
    }

    @Test
    fun toCoordinate() {
        val center = Coordinate(0.0, 0.0)
        val projection = AzimuthalEquidistantProjection(center)
        assertThat(center).isCloseTo(projection.toCoordinate(Vector2.zero), 1f)
        assertThat(center.plus(100.0, Bearing(0f))).isCloseTo(projection.toCoordinate(Vector2(0f, 100f)), 1f)
        assertThat(center.plus(100.0, Bearing(180f))).isCloseTo(projection.toCoordinate(Vector2(0f, -100f)), 1f)
        assertThat(center.plus(100.0, Bearing(90f))).isCloseTo(projection.toCoordinate(Vector2(100f, 0f)), 1f)
        assertThat(center.plus(100.0, Bearing(270f))).isCloseTo(projection.toCoordinate(Vector2(-100f, 0f)), 1f)
    }

    @Test
    fun toCoordinateScaled(){
        val center = Coordinate(0.0, 0.0)
        val projection = AzimuthalEquidistantProjection(center, scale = 2f)
        assertThat(center).isCloseTo(projection.toCoordinate(Vector2.zero), 1f)
        assertThat(center.plus(100.0, Bearing(0f))).isCloseTo(projection.toCoordinate(Vector2(0f, 200f)), 1f)
        assertThat(center.plus(100.0, Bearing(180f))).isCloseTo(projection.toCoordinate(Vector2(0f, -200f)), 1f)
        assertThat(center.plus(100.0, Bearing(90f))).isCloseTo(projection.toCoordinate(Vector2(200f, 0f)), 1f)
        assertThat(center.plus(100.0, Bearing(270f))).isCloseTo(projection.toCoordinate(Vector2(-200f, 0f)), 1f)
    }

    @Test
    fun toCoordinateFlipped(){
        val center = Coordinate(0.0, 0.0)
        val projection = AzimuthalEquidistantProjection(center, isYFlipped = true)
        assertThat(center).isCloseTo(projection.toCoordinate(Vector2.zero), 1f)
        assertThat(center.plus(100.0, Bearing(0f))).isCloseTo(projection.toCoordinate(Vector2(0f, -100f)), 1f)
        assertThat(center.plus(100.0, Bearing(180f))).isCloseTo(projection.toCoordinate(Vector2(0f, 100f)), 1f)
        assertThat(center.plus(100.0, Bearing(90f))).isCloseTo(projection.toCoordinate(Vector2(100f, 0f)), 1f)
        assertThat(center.plus(100.0, Bearing(270f))).isCloseTo(projection.toCoordinate(Vector2(-100f, 0f)), 1f)
    }

    @Test
    fun toCoordinateOffset(){
        val center = Coordinate(0.0, 0.0)
        val projection = AzimuthalEquidistantProjection(center, centerPixel = Vector2(10f, 10f))
        assertThat(center).isCloseTo(projection.toCoordinate(Vector2(10f, 10f)), 1f)
        assertThat(center.plus(100.0, Bearing(0f))).isCloseTo(projection.toCoordinate(Vector2(10f, 110f)), 1f)
        assertThat(center.plus(100.0, Bearing(180f))).isCloseTo(projection.toCoordinate(Vector2(10f, -90f)), 1f)
        assertThat(center.plus(100.0, Bearing(90f))).isCloseTo(projection.toCoordinate(Vector2(110f, 10f)), 1f)
        assertThat(center.plus(100.0, Bearing(270f))).isCloseTo(projection.toCoordinate(Vector2(-90f, 10f)), 1f)
    }
}