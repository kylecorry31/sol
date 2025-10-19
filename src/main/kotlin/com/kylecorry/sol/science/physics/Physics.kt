package com.kylecorry.sol.science.physics

import com.kylecorry.sol.math.*
import com.kylecorry.sol.math.calculus.RungeKutta4thOrderSolver
import com.kylecorry.sol.math.interpolation.Interpolator
import com.kylecorry.sol.math.optimization.IOptimizer
import com.kylecorry.sol.science.geology.Geology
import com.kylecorry.sol.units.*
import java.time.Duration
import kotlin.math.absoluteValue

object Physics : IPhysicsService {

    override fun fallHeight(time: Duration, gravity: Float): Distance {
        val seconds = time.toMillis() / 1000f
        return Distance.from(0.5f * gravity * seconds * seconds, DistanceUnits.Meters)
    }

    override fun isMetal(magneticField: Vector3, threshold: Float): Boolean {
        return isMetal(magneticField.magnitude(), threshold)
    }

    override fun isMetal(fieldStrength: Float, threshold: Float): Boolean {
        return fieldStrength >= threshold
    }

    override fun removeGeomagneticField(
        rawMagneticField: Vector3,
        geomagneticField: Vector3,
        orientationChange: Quaternion?
    ): Vector3 {
        val updatedGeo = orientationChange?.rotate(geomagneticField)
            ?: (rawMagneticField.normalize() * geomagneticField.magnitude())
        return rawMagneticField - updatedGeo
    }

    override fun getMetalDirection(
        magneticField: Vector3,
        gravity: Vector3
    ): Pair<Bearing, Bearing> {
        val azimuth = Geology.getAzimuth(gravity, magneticField)
        return azimuth to azimuth.inverse()
    }

    override fun getTrajectory2D(
        initialPosition: Vector2,
        initialVelocity: Vector2,
        dragModel: DragModel,
        timeStep: Float,
        maxTime: Float,
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
            Vector.from(velocity.x, velocity.y, drag.x, drag.y - Geology.GRAVITY)
        }

        for (result in results) {
            val time = result.first
            val position = Vector2(result.second[0], result.second[1])
            val velocity = Vector2(result.second[2], result.second[3])
            trajectory.add(TrajectoryPoint2D(time, position, velocity))
        }

        return trajectory
    }

    override fun getVelocityVectorForImpact(
        targetPosition: Vector2,
        velocity: Float,
        initialPosition: Vector2,
        dragModel: DragModel,
        timeStep: Float,
        maxTime: Float,
        minAngle: Float,
        maxAngle: Float,
        optimizer: IOptimizer,
        getInterpolator: (points: List<Vector2>) -> Interpolator
    ): Vector2 {
        val bestAngle = optimizer.optimize(
            Range(minAngle.toDouble(), maxAngle.toDouble()),
            maximize = false
        ) { angle, _ ->
            val initialVelocity =
                Vector2(
                    velocity * SolMath.cosDegrees(angle.toFloat()),
                    velocity * SolMath.sinDegrees(angle.toFloat())
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
            velocity * SolMath.cosDegrees(bestAngle.first.toFloat()),
            velocity * SolMath.sinDegrees(bestAngle.first.toFloat())
        )
    }

    override fun getKineticEnergy(mass: Weight, speed: Speed): Energy {
        val massKg = mass.convertTo(WeightUnits.Kilograms).value
        val speedMps = speed.convertTo(DistanceUnits.Meters, TimeUnits.Seconds).speed
        val joules = 0.5f * massKg * speedMps * speedMps
        return Energy.from(joules, EnergyUnits.Joules)
    }

}

data class TrajectoryPoint2D(
    val time: Float,
    val position: Vector2,
    val velocity: Vector2
)

interface DragModel {
    fun getDragAcceleration(velocity: Vector2): Vector2
}

class NoDragModel : DragModel {
    override fun getDragAcceleration(velocity: Vector2): Vector2 {
        return Vector2.zero
    }

}