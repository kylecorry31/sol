package com.kylecorry.sol.science.astronomy.navigation

import com.kylecorry.sol.math.Vector3Precise
import com.kylecorry.sol.math.geometry.Rotation
import com.kylecorry.sol.science.optics.PinholeCamera
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * Generates star photos from the star catalog with lens, sensor and detection errors
 */
class SyntheticSky(private val random: Random) {

    data class Errors(
        val limitingMagnitude: Float = 3.5f,
        val pixelNoise: Double = 0.5,
        val focalError: Double = 0.03,
        val maxK1: Double = 0.03,
        val tiltNoiseDegrees: Double = 0.0,
        val falseStars: Int = 2,
        val dropout: Double = 0.1
    )

    class Photo(val image: SkyImage, val truth: Coordinate, val deviceToEnu: Rotation)

    val nominalCamera = PinholeCamera.fromVerticalFieldOfView(3000, 4000, 66.0)

    fun randomLocation(): Coordinate {
        return Coordinate(random.nextDouble(-55.0, 60.0), random.nextDouble(-180.0, 180.0))
    }

    fun randomTime(): ZonedDateTime {
        val start = Instant.parse("2026-01-01T00:00:00Z")
        return ZonedDateTime.ofInstant(start.plusSeconds(random.nextLong(0, 365L * 24 * 3600)), ZoneOffset.UTC)
    }

    /**
     * The device to ENU rotation for a camera pointed at the azimuth / altitude and rolled about its axis
     */
    fun orientation(azimuth: Double, altitude: Double, roll: Double): Rotation {
        val pointing = HorizonVectors.toVector(azimuth, altitude)
        val z = pointing
        val reference = if (altitude > 89.0) HorizonVectors.toVector(azimuth, 0.0) else HorizonVectors.up
        var y = (pointing * reference.dot(pointing) - reference).normalize()
        y = Rotation.fromAxisAngle(pointing, roll).rotate(y)
        val x = y.cross(z)
        return Rotation.fromMatrix(
            doubleArrayOf(
                x.x, y.x, z.x,
                x.y, y.y, z.y,
                x.z, y.z, z.z
            )
        )
    }

    fun randomOrientation(): Rotation {
        return orientation(random.nextDouble(0.0, 360.0), random.nextDouble(35.0, 80.0), random.nextDouble(-30.0, 30.0))
    }

    fun generate(
        errors: Errors,
        location: Coordinate = randomLocation(),
        time: ZonedDateTime = randomTime(),
        deviceToEnu: Rotation = randomOrientation(),
        gravityBias: Vector3Precise = Vector3Precise.zero,
        actualCamera: PinholeCamera? = null
    ): Photo {
        val camera = actualCamera ?: nominalCamera.copy(
            focalLength = nominalCamera.focalLength * (1 + uniform(errors.focalError)),
            k1 = uniform(errors.maxK1)
        )
        val enuToDevice = deviceToEnu.inverse()
        val stars = mutableListOf<ImageStar>()
        for (position in SkyCatalog.getPositions(SkyCatalog.getBodies(errors.limitingMagnitude), time, location, true, 0.0)) {
            if (random.nextDouble() < errors.dropout) continue
            val (px, py) = camera.toPixel(enuToDevice.rotate(position.vector)) ?: continue
            val x = px + gaussian() * errors.pixelNoise
            val y = py + gaussian() * errors.pixelNoise
            if (x < 0 || y < 0 || x >= camera.width || y >= camera.height) continue
            val brightness = 10.0.pow(-0.4 * position.body.magnitude) * (1 + 0.2 * gaussian())
            stars.add(ImageStar(x, y, brightness.coerceAtLeast(1e-4)))
        }
        repeat(errors.falseStars) {
            stars.add(
                ImageStar(
                    random.nextDouble(0.0, camera.width.toDouble()),
                    random.nextDouble(0.0, camera.height.toDouble()),
                    10.0.pow(-0.4 * random.nextDouble(0.0, 3.0))
                )
            )
        }
        stars.shuffle(random)

        val trueUp = enuToDevice.rotate(HorizonVectors.up)
        val noise = perpendicular(trueUp, errors.tiltNoiseDegrees * gaussian())
        val up = tilt(tilt(trueUp, gravityBias), noise)
        return Photo(SkyImage(stars, nominalCamera, up, time), location, deviceToEnu)
    }

    fun nearby(location: Coordinate, km: Double): Coordinate {
        return location.plus(km * 1000 * random.nextDouble(0.5, 1.0), Bearing.from(random.nextDouble(0.0, 360.0).toFloat()))
    }

    /**
     * A rotation vector (axis * degrees) perpendicular to the direction, in a random direction
     */
    fun perpendicular(direction: Vector3Precise, degrees: Double): Vector3Precise {
        val randomDirection = Vector3Precise(gaussian(), gaussian(), gaussian()).normalize()
        return direction.cross(randomDirection).normalize() * degrees
    }

    fun tilt(vector: Vector3Precise, rotationVector: Vector3Precise): Vector3Precise {
        val degrees = rotationVector.magnitude()
        if (degrees < 1e-12) return vector
        return Rotation.fromAxisAngle(rotationVector, degrees).rotate(vector)
    }

    fun gaussian(): Double {
        return sqrt(-2 * ln(random.nextDouble(1e-12, 1.0))) * cos(2 * Math.PI * random.nextDouble())
    }

    private fun uniform(max: Double): Double {
        return if (max <= 0) 0.0 else random.nextDouble(-max, max)
    }
}
