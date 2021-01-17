package com.kylecorry.trailsensecore.infrastructure.sensors.battery

import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface IBattery : ISensor {
    val percent: Float
    val capacity: Float
    val health: BatteryHealth
    val voltage: Float
    val current: Float
    val chargingMethod: BatteryChargingMethod
    val chargingStatus: BatteryChargingStatus
}