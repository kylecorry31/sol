package com.kylecorry.sol.science.optics

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.DistanceUnits
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class OpticsTest {

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0, 0, 0, 0, 0, NaN, NaN",
        "0, 0, 0, 1000, 1000, 500, 500, NaN, NaN",
        "0, 0, 1, 1000, 1000, 500, 500, 500, 500",
        "1, 2, 3, 1000, 1000, 500, 500, 833.333, 1166.666",
        "1, 2, 1, 1000, 1000, 500, 500, 1500, 2500",
        "1, 2, 2, 1000, 2000, 500, 600, 1000, 2600",
        "-1, -2, 3, 1000, 1000, 500, 500, 166.666, -166.666",
        "-1, -2, 1, 1000, 1000, 500, 500, -500, -1500",
        "-1, -2, 2, 1000, 2000, 500, 600, 0, -1400",
        "-1, -2, -1, 1000, 1000, 500, 500, 1500, 2500",
    )
    fun perspectiveProjection(
        x: Float,
        y: Float,
        z: Float,
        fx: Float,
        fy: Float,
        cx: Float,
        cy: Float,
        expectedX: Float,
        expectedY: Float
    ) {
        val point = Vector3(x, y, z)
        val projection = Optics.perspectiveProjection(point, Vector2(fx, fy), Vector2(cx, cy))
        assertEquals(expectedX, projection.x, 0.001f)
        assertEquals(expectedY, projection.y, 0.001f)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0, 0, 0, 0, 0, NaN, NaN, NaN",
        "0, 0, 0, 0, 0, 0, 0, NaN, NaN, NaN",
        "500, 500, 1, 1000, 1000, 500, 500, 0, 0, 1",
        "833.333, 1166.666, 3.74165, 1000, 1000, 500, 500, 1, 2, 3",
        "1500, 2500, 2.44948, 1000, 1000, 500, 500, 1, 2, 1",
        "1000, 2600, 3, 1000, 2000, 500, 600, 1, 2, 2",
        "166.666, -166.666, 3.74165, 1000, 1000, 500, 500, -1, -2, 3",
        "-500, -1500, 2.44948, 1000, 1000, 500, 500, -1, -2, 1",
        "0, -1400, 3, 1000, 2000, 500, 600, -1, -2, 2",
    )
    fun inversePerspectiveProjection(
        screenX: Float,
        screenY: Float,
        distance: Float,
        fx: Float,
        fy: Float,
        cx: Float,
        cy: Float,
        expectedX: Float,
        expectedY: Float,
        expectedZ: Float
    ) {
        val expected = Vector3(expectedX, expectedY, expectedZ)
        val point = Vector2(screenX, screenY)
        val projection = Optics.inversePerspectiveProjection(point, Vector2(fx, fy), Vector2(cx, cy), distance)
        assertEquals(expected.x, projection.x, 0.001f)
        assertEquals(expected.y, projection.y, 0.001f)
        assertEquals(expected.z, projection.z, 0.001f)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0",
        "1, 1, 53.1301",
        "1, 2, 28.0725",
        "2, 1, 90",
    )
    fun getAngularSize(diameter: Float, distance: Float, expected: Float) {
        assertEquals(expected, Optics.getAngularSize(diameter, distance), 0.001f)

        // Test the distance overload
        assertEquals(
            expected,
            Optics.getAngularSize(
                Distance.meters(diameter).convertTo(DistanceUnits.Feet),
                Distance.meters(distance).convertTo(DistanceUnits.Miles)
            ),
            0.001f
        )
    }

    @ParameterizedTest
    @CsvSource(
        "1, 2, 1000, 500",
        "2, 1, 1000, 2000",
        "1, 2, 2000, 1000",
        "2, 1, 2000, 4000",
        "1, 2, 500, 250",
        "2, 1, 500, 1000",
        "1000, 2000, 2000, 1000"
    )
    fun getFocalLengthPixels(focalLength: Float, sensorSize: Float, sensorSizePixels: Int, expected: Float) {
        assertEquals(expected, Optics.getFocalLengthPixels(focalLength, sensorSize, sensorSizePixels), 0.001f)
    }

    @ParameterizedTest
    @CsvSource(
        "1, 2, 90",
        "2, 1, 28.0725",
        "1, 1, 53.1301",
        "1000, 2000, 90",
        "2000, 1000, 28.0725",
        "1000, 1000, 53.1301",
    )
    fun getFieldOfView(focalLength: Float, sensorSize: Float, expected: Float) {
        assertEquals(expected, Optics.getFieldOfView(focalLength, sensorSize), 0.001f)
    }

    @ParameterizedTest
    @CsvSource(
        "90, 2, 1",
        "28.0725, 1, 2",
        "53.1301, 1, 1",
        "90, 2000, 1000",
        "28.0725, 1000, 2000",
        "53.1301, 1000, 1000",
    )
    fun getFocalLength(fieldOfView: Float, viewSize: Float, expected: Float) {
        assertEquals(expected, Optics.getFocalLength(fieldOfView, viewSize), 0.001f)
    }

    @ParameterizedTest
    @CsvSource(
        "8148, 181",
        "5600, 150"
    )
    fun beamDistance(candela: Float, distanceMeters: Float) {
        val beamDistance = Optics.lightBeamDistance(candela)
        assertEquals(distanceMeters, beamDistance.distance, 0.5f)
        assertEquals(DistanceUnits.Meters, beamDistance.units)
    }

    @ParameterizedTest
    @CsvSource(
        "8148, 181, 0.25",
        "5600, 150, 0.25"
    )
    fun luxAtDistance(candela: Float, meters: Float, lux: Float) {
        val distance = Distance.meters(meters)
        val actualLux = Optics.luxAtDistance(candela, distance)
        assertEquals(lux, actualLux, 0.1f)
    }

}