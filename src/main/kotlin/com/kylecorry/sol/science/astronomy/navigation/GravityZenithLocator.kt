package com.kylecorry.sol.science.astronomy.navigation

import com.kylecorry.sol.math.Vector3Precise
import com.kylecorry.sol.math.geometry.Rotation
import com.kylecorry.sol.science.geology.Geology
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate

/**
 * Finds the location where gravity's up direction is the zenith. The rotation from the camera to
 * the sky is fit from every identified star, gravity is mapped through it, and the estimate moves
 * to where that direction is overhead. Repeats until it converges.
 *
 * The result is an astronomic location: see Geophysics.getGeodeticLocation to convert it to geodetic.
 */
internal class GravityZenithLocator(private val withRefraction: Boolean = true) {

    fun getLocation(
        image: SkyImage,
        identification: StarIdentification,
        approximateLocation: Coordinate,
        up: Vector3Precise = checkNotNull(image.up) { "Gravity is required" }
    ): Coordinate? {
        if (identification.stars.size < 2) return null
        val rays = identification.stars.map { identification.camera.toRay(it.star.x, it.star.y) }
        val bodies = identification.stars.map { it.body }
        val normalizedUp = up.normalize()
        var location = approximateLocation
        var iteration = 0
        var moved = Double.MAX_VALUE
        while (iteration < MAX_ITERATIONS && moved >= CONVERGENCE_METERS) {
            val targets = SkyCatalog.getPositions(bodies, image.time, location, withRefraction)
                .map { position -> position.vector }
            val rotation = Rotation.fit(rays, targets)
            val (azimuth, altitude) = HorizonVectors.fromVector(rotation.rotate(normalizedUp))
            moved = Math.toRadians(90 - altitude) * Geology.EARTH_AVERAGE_RADIUS
            location = location.plus(moved, Bearing.from(azimuth.toFloat()))
            iteration++
        }
        return location
    }

    companion object {
        private const val MAX_ITERATIONS = 12
        private const val CONVERGENCE_METERS = 0.5
    }
}
