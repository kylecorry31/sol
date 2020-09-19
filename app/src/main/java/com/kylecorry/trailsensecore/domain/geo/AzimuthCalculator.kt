package com.kylecorry.trailsensecore.domain.geo

import com.kylecorry.trailsensecore.domain.math.Vector3
import com.kylecorry.trailsensecore.domain.math.toDegrees
import kotlin.math.atan2

// From https://stackoverflow.com/questions/16317599/android-compass-that-can-compensate-for-tilt-and-pitch

internal object AzimuthCalculator {

    fun calculate(gravity: Vector3, magneticField: Vector3): Bearing? {
        // Gravity
        val normGravity = gravity.normalize()
        val normMagField = magneticField.normalize()

        // East vector
        val east = normMagField.cross(normGravity)
        val normEast = east.normalize()

        // Magnitude check
        val eastMagnitude = east.magnitude()
        val gravityMagnitude = gravity.magnitude()
        val magneticMagnitude = magneticField.magnitude()
        if (gravityMagnitude * magneticMagnitude * eastMagnitude < 0.1f) {
            return null
        }

        // North vector
        val dotProduct = normGravity.dot(normMagField)
        val north = normMagField.minus(normGravity * dotProduct)
        val normNorth = north.normalize()

        // Azimuth
        // NB: see https://math.stackexchange.com/questions/381649/whats-the-best-3d-angular-co-ordinate-system-for-working-with-smartfone-apps
        val sin = normEast.y - normNorth.x
        val cos = normEast.x + normNorth.y
        val azimuth = if (!(sin == 0f && sin == cos)) atan2(sin, cos) else 0f

        if (azimuth.isNaN()){
            return null
        }

        return Bearing(azimuth.toDegrees())
    }

}