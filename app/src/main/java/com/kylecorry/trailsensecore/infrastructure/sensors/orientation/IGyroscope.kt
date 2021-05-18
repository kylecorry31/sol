package com.kylecorry.trailsensecore.infrastructure.sensors.orientation

interface IGyroscope: IRotationSensor {
    fun calibrate()
}