package com.kylecorry.trailsensecore.science.physics

import com.kylecorry.andromeda.core.math.Quaternion
import com.kylecorry.andromeda.core.math.Vector3
import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.andromeda.core.units.DistanceUnits
import com.kylecorry.trailsensecore.science.geology.GeologyService
import java.time.Duration
import kotlin.math.sqrt

class PhysicsService : IPhysicsService {

    override fun fallHeight(time: Duration): Distance {
        val seconds = time.toMillis() / 1000f
        return Distance(0.5f * GeologyService.GRAVITY * seconds * seconds, DistanceUnits.Meters)
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
        val azimuth = GeologyService().getAzimuth(gravity, magneticField)
        return azimuth to azimuth.inverse()
    }

    override fun luxToCandela(lux: Float, distance: Distance): Float {
        val meters = distance.meters().distance
        return lux * meters * meters
    }

    override fun luxAtDistance(candela: Float, distance: Distance): Float {
        val meters = distance.meters().distance
        return candela / (meters * meters)
    }

    override fun lightBeamDistance(candela: Float): Distance {
        return Distance.meters(sqrt(candela * 4))
    }

}