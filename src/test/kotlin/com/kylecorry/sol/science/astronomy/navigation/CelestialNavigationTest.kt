package com.kylecorry.sol.science.astronomy.navigation

import com.kylecorry.sol.math.Vector3Precise
import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.science.geophysics.Geophysics
import com.kylecorry.sol.science.geophysics.VerticalDeflection
import com.kylecorry.sol.units.Coordinate
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.random.Random

class CelestialNavigationTest {

    @Test
    fun identifiesStars() {
        val sky = SyntheticSky(Random(1))
        repeat(5) {
            val photo = sky.generate(SyntheticSky.Errors(falseStars = 0, dropout = 0.0))
            val identification = Astronomy.identifyStars(photo.image, sky.nearby(photo.truth, 800.0))
            assertNotNull(identification)
            assertTrue(identification!!.stars.size >= photo.image.stars.size * 0.9)
            assertTrue(identification.rmsDegrees < 0.05)
        }
    }

    @Test
    fun getLocationWithPerfectGravity() {
        val sky = SyntheticSky(Random(2))
        repeat(5) {
            val photo = sky.generate(SyntheticSky.Errors(pixelNoise = 0.0, focalError = 0.0, maxK1 = 0.0, falseStars = 0, dropout = 0.0))
            val location = Astronomy.getLocationFromStars(photo.image, sky.nearby(photo.truth, 800.0))
            assertNotNull(location)
            assertTrue(location!!.distanceTo(photo.truth) < 500f, "Error: ${location.distanceTo(photo.truth)} m")
        }
    }

    @Test
    fun getLocationWithNoisyGravity() {
        val sky = SyntheticSky(Random(3))
        val errors = (0 until 10).map {
            val photo = sky.generate(SyntheticSky.Errors(tiltNoiseDegrees = 0.05))
            val location = Astronomy.getLocationFromStars(photo.image, sky.nearby(photo.truth, 800.0))
            location!!.distanceTo(photo.truth) / 1000.0
        }.sorted()
        assertTrue(errors[errors.size / 2] < 15, "Median error: ${errors[errors.size / 2]} km")
    }

    @Test
    fun getLocationWithDeflection() {
        val sky = SyntheticSky(Random(4))
        val geodetic = Coordinate(41.824, -71.4128)
        val deflection = VerticalDeflection(-5.31, -2.94)
        val photo = sky.generate(
            SyntheticSky.Errors(pixelNoise = 0.0, focalError = 0.0, maxK1 = 0.0, falseStars = 0, dropout = 0.0),
            location = Geophysics.getAstronomicLocation(geodetic, deflection)
        )
        val location = Astronomy.getLocationFromStars(photo.image, sky.nearby(geodetic, 300.0), deflection = { deflection })
        assertTrue(location!!.distanceTo(geodetic) < 100f, "Error: ${location.distanceTo(geodetic)} m")
    }

    @Test
    fun getOrientationIgnoresGravityError() {
        val sky = SyntheticSky(Random(5))
        repeat(10) {
            val bias = sky.perpendicular(Vector3Precise(0.0, 0.0, 1.0), 0.5)
            val photo = sky.generate(SyntheticSky.Errors(), gravityBias = bias)
            val identification = Astronomy.getOrientationFromStars(photo.image, photo.truth)
            assertNotNull(identification)
            val axis = Vector3Precise(0.0, 0.0, 1.0)
            val estimated = HorizonVectors.fromVector(identification!!.rotation.rotate(axis)).first
            val actual = HorizonVectors.fromVector(photo.deviceToEnu.rotate(axis)).first
            val error = abs(((estimated - actual + 540) % 360) - 180)
            assertTrue(error < 0.05, "Heading error: $error°")
        }
    }
}
