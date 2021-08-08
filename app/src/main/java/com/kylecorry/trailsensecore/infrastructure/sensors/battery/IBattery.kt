package com.kylecorry.trailsensecore.infrastructure.sensors.battery

import com.kylecorry.sense.ISensor

interface IBattery : ISensor {
    val percent: Float
    val capacity: Float
    val maxCapacity: Float
    val health: BatteryHealth
    val voltage: Float
    val current: Float
    val chargingMethod: BatteryChargingMethod
    val chargingStatus: BatteryChargingStatus
}