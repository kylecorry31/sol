package com.kylecorry.sol.science.physics

import com.kylecorry.sol.math.Quaternion
import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.science.geology.Geology
import com.kylecorry.sol.units.*
import java.time.Duration

object Physics : IPhysicsService {

    override fun fallHeight(time: Duration, gravity: Float): Distance {
        val seconds = time.toMillis() / 1000f
        return Distance(0.5f * gravity * seconds * seconds, DistanceUnits.Meters)
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

        var x = initialPosition.x
        var y = initialPosition.y
        var vx = initialVelocity.x
        var vy = initialVelocity.y
        var t = 0f
        var g = Geology.GRAVITY

        trajectory.add(TrajectoryPoint2D(t, Vector2(x, y), Vector2(vx, vy)))

        while (t < maxTime) {
            val drag = dragModel.getDragAcceleration(Vector2(vx, vy))
            vx += drag.x * timeStep
            vy += (drag.y - g) * timeStep
            x += vx * timeStep
            y += vy * timeStep
            t += timeStep

            trajectory.add(TrajectoryPoint2D(t, Vector2(x, y), Vector2(vx, vy)))
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
        angleStep: Float,
        tolerance: Float,
    ): Vector2 {
        var currentAngle = minAngle
        var bestAngle = minAngle
        var bestError = Float.MAX_VALUE
        var bestVelocity = Vector2.zero

        while (currentAngle <= maxAngle) {
            val initialVelocity =
                Vector2(velocity * SolMath.cosDegrees(currentAngle), velocity * SolMath.sinDegrees(currentAngle))
            val trajectory = getTrajectory2D(initialPosition, initialVelocity, dragModel, timeStep, maxTime)

            for (point in trajectory) {
                val error = (point.position - targetPosition).magnitude()
                if (error < bestError) {
                    bestError = error
                    bestAngle = currentAngle
                    bestVelocity = initialVelocity
                }
                if (error < tolerance) {
                    return initialVelocity
                }
            }
            currentAngle += angleStep
        }

        return bestVelocity
    }

    override fun getKineticEnergy(mass: Weight, speed: Speed): Energy {
        val massKg = mass.convertTo(WeightUnits.Kilograms).weight
        val speedMps = speed.convertTo(DistanceUnits.Meters, TimeUnits.Seconds).speed
        val joules = 0.5f * massKg * speedMps * speedMps
        return Energy(joules, EnergyUnits.Joules)
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