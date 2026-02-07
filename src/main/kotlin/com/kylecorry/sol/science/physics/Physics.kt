package com.kylecorry.sol.science.physics

import com.kylecorry.sol.math.*
import com.kylecorry.sol.math.analysis.Trigonometry
import com.kylecorry.sol.math.calculus.RungeKutta4thOrderSolver
import com.kylecorry.sol.math.interpolation.Interpolator
import com.kylecorry.sol.math.interpolation.LinearInterpolator
import com.kylecorry.sol.math.optimization.HillClimbingOptimizer
import com.kylecorry.sol.math.optimization.IOptimizer
import com.kylecorry.sol.science.geophysics.Geophysics
import com.kylecorry.sol.units.*
import java.time.Duration
import kotlin.math.absoluteValue

object Physics {

    fun fallHeight(time: Duration, gravity: Float = Geophysics.GRAVITY): Distance {
        val seconds = time.toMillis() / 1000f
        return Distance.from(0.5f * gravity * seconds * seconds, DistanceUnits.Meters)
    }

    fun isMetal(magneticField: Vector3, threshold: Float = 65f): Boolean {
        return isMetal(magneticField.magnitude(), threshold)
    }

    fun isMetal(fieldStrength: Float, threshold: Float = 65f): Boolean {
        return fieldStrength >= threshold
    }

    /**
     * Removes the geomagnetic field from a raw magnetometer reading.
     * The orientation changes is the changes in phone orientation from the point the geomagnetic field was taken.
     */
    fun removeGeomagneticField(
        rawMagneticField: Vector3,
        geomagneticField: Vector3,
        orientationChange: Quaternion? = null
    ): Vector3 {
        val updatedGeo = orientationChange?.rotate(geomagneticField)
            ?: (rawMagneticField.normalize() * geomagneticField.magnitude())
        return rawMagneticField - updatedGeo
    }

    /**
     * Calculates the direction to a piece of metal
     * @return The poles pointing to and away from the metal (unable to determine which)
     */
    fun getMetalDirection(
        magneticField: Vector3,
        gravity: Vector3
    ): Pair<Bearing, Bearing> {
        val azimuth = Geophysics.getAzimuth(gravity, magneticField)
        return azimuth to azimuth.inverse()
    }

    /**
     * Calculates the trajectory of a projectile in 2D space.
     */
    fun getTrajectory2D(
        initialPosition: Vector2 = Vector2.zero,
        initialVelocity: Vector2 = Vector2.zero,
        dragModel: DragModel = NoDragModel(),
        timeStep: Float = 0.01f,
        maxTime: Float = 10f,
    ): List<TrajectoryPoint2D> {
        val trajectory = mutableListOf<TrajectoryPoint2D>()

        val solver = RungeKutta4thOrderSolver()
        val results = solver.solve(
            Range(0f, maxTime),
            timeStep,
            Vector.from(
                initialPosition.x,
                initialPosition.y,
                initialVelocity.x,
                initialVelocity.y
            ),
        ) { _, v ->
            val velocity = Vector2(v[2], v[3])
            val drag = dragModel.getDragAcceleration(velocity)
            Vector.from(velocity.x, velocity.y, drag.x, drag.y - Geophysics.GRAVITY)
        }

        for (result in results) {
            val time = result.first
            val position = Vector2(result.second[0], result.second[1])
            val velocity = Vector2(result.second[2], result.second[3])
            trajectory.add(TrajectoryPoint2D(time, position, velocity))
        }

        return trajectory
    }

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
        optimizer: IOptimizer = HillClimbingOptimizer(0.001, initialValue = 0.0 to 0.0),
        getInterpolator: (points: List<Vector2>) -> Interpolator = { points ->
            LinearInterpolator(points)
        }
    ): Vector2 {
        val bestAngle = optimizer.optimize(
            Range(minAngle.toDouble(), maxAngle.toDouble()),
            maximize = false
        ) { angle, _ ->
            val initialVelocity =
                Vector2(
                    velocity * Trigonometry.cosDegrees(angle.toFloat()),
                    velocity * Trigonometry.sinDegrees(angle.toFloat())
                )
            val trajectory =
                getTrajectory2D(
                    initialPosition,
                    initialVelocity,
                    dragModel,
                    timeStep,
                    maxTime
                )
            val interpolated =
                getInterpolator(trajectory.map { it.position }).interpolate(
                    targetPosition.x
                )
            (interpolated - targetPosition.y).absoluteValue.toDouble()
        }
        return Vector2(
            velocity * Trigonometry.cosDegrees(bestAngle.first.toFloat()),
            velocity * Trigonometry.sinDegrees(bestAngle.first.toFloat())
        )
    }

    /**
     * Calculates the kinetic energy of an object moving at a given speed.
     */
    fun getKineticEnergy(mass: Weight, speed: Speed): Energy {
        val massKg = mass.convertTo(WeightUnits.Kilograms).value
        val speedMps = speed.convertTo(DistanceUnits.Meters, TimeUnits.Seconds).speed
        val joules = 0.5f * massKg * speedMps * speedMps
        return Energy.from(joules, EnergyUnits.Joules)
    }

}