package com.kylecorry.trailsensecore.science.physics

import com.kylecorry.trailsensecore.math.Quaternion
import com.kylecorry.trailsensecore.math.Vector3
import com.kylecorry.trailsensecore.units.Bearing
import com.kylecorry.trailsensecore.units.Distance
import java.time.Duration

interface IPhysicsService {
    // Kinematics
    fun fallHeight(time: Duration): Distance

    // Magnetic fields
    fun isMetal(magneticField: Vector3, threshold: Float = 65f): Boolean
    fun isMetal(fieldStrength: Float, threshold: Float = 65f): Boolean

    /**
     * Removes the geomagnetic field from a raw magnetometer reading.
     * The orientation changes is the changes in phone orientation from the point the geomagnetic field was taken.
     */
    fun removeGeomagneticField(
        rawMagneticField: Vector3,
        geomagneticField: Vector3,
        orientationChange: Quaternion? = null
    ): Vector3

    /**
     * Calculates the direction to a piece of metal
     * @return The poles pointing to and away from the metal (unable to determine which)
     */
    fun getMetalDirection(magneticField: Vector3, gravity: Vector3): Pair<Bearing, Bearing>

    // Light
    fun luxToCandela(lux: Float, distance: Distance): Float

    fun luxAtDistance(candela: Float, distance: Distance): Float

    fun lightBeamDistance(candela: Float): Distance

}