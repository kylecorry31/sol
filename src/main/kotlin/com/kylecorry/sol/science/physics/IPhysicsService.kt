package com.kylecorry.sol.science.physics

import com.kylecorry.sol.math.Quaternion
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.science.geology.Geology
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Energy
import com.kylecorry.sol.units.Speed
import com.kylecorry.sol.units.Weight
import java.time.Duration

interface IPhysicsService {
    // Kinematics
    fun fallHeight(time: Duration, gravity: Float = Geology.GRAVITY): Distance

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

    /**
     * Calculates the trajectory of a projectile in 2D space.
     */
    fun getTrajectory2D(
        initialPosition: Vector2 = Vector2.zero,
        initialVelocity: Vector2 = Vector2.zero,
        dragModel: DragModel = NoDragModel(),
        timeStep: Float = 0.01f,
        maxTime: Float = 10f,
    ): List<TrajectoryPoint2D>

    /**
     * Calculates the required velocity vector for an impact at a target position with the given velocity.
     */
    fun getVelocityVectorForImpact(
        targetPosition: Vector2,
        velocity: Float,
        initialPosition: Vector2 = Vector2.zero,
        dragModel: DragModel = NoDragModel(),
        timeStep: Float = 0.01f,
        maxTime: Float = 10f,
        minAngle: Float = 0f,
        maxAngle: Float = 90f,
        angleStep: Float = 1f,
        tolerance: Float = 0.01f,
    ): Vector2

    /**
     * Calculates the kinetic energy of an object moving at a given speed.
     */
    fun getKineticEnergy(mass: Weight, speed: Speed): Energy
}