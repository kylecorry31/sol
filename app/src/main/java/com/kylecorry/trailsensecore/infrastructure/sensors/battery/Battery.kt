package com.kylecorry.trailsensecore.infrastructure.sensors.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.getSystemService
import com.kylecorry.trailsensecore.infrastructure.sensors.AbstractSensor


class Battery(private val context: Context) : IBattery, AbstractSensor() {
    override val percent: Float
        get() {
            val pct = (getInt(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 0).toFloat()
            return if (pct <= 0f) {
                _percent
            } else {
                pct
            }
        }
    override val capacity: Float
        get() {
            val cap =
                (getInt(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) ?: 0).toFloat() * 0.001f
            return if (cap <= 0f) {
                0f
            } else {
                cap
            }
        }
    override val health: BatteryHealth
        get() = _health
    override val charging: Boolean
        get() = _charging
    override val voltage: Float
        get() = _voltage
    override val current: Float
        get() {
            return (getInt(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0).toFloat() * 0.001f
        }
    override val chargingMethod: BatteryChargingMethod
        get() = _chargingMethod
    override val hasValidReading: Boolean
        get() = hasReading

    private val batteryManager: BatteryManager? by lazy { context.getSystemService() }

    private var hasReading = false
    private var _percent = 0f
    private var _charging = false
    private var _voltage = 0f
    private var _health = BatteryHealth.Unknown
    private var _chargingMethod = BatteryChargingMethod.Unknown

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
            _percent = 100 * level / scale.toFloat()
            val chargingType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            _charging = chargingType != 0
            _chargingMethod = when (chargingType) {
                BatteryManager.BATTERY_PLUGGED_AC -> BatteryChargingMethod.AC
                BatteryManager.BATTERY_PLUGGED_USB -> BatteryChargingMethod.USB
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> BatteryChargingMethod.Wireless
                else -> BatteryChargingMethod.NotCharging
            }
            _voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toFloat() / 1000f
            _health = when (intent.getIntExtra(
                BatteryManager.EXTRA_HEALTH,
                BatteryManager.BATTERY_HEALTH_UNKNOWN
            )) {
                BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.Cold
                BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.Dead
                BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.Good
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.Overheat
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OverVoltage
                else -> BatteryHealth.Unknown
            }

            hasReading = true
            notifyListeners()
        }
    }

    private fun getInt(property: Int): Int? {
        val value = batteryManager?.getIntProperty(property) ?: return null
        if (value == Long.MIN_VALUE.toInt() || value == Int.MIN_VALUE) {
            return null
        }
        return value
    }

    override fun startImpl() {
        context.registerReceiver(receiver, IntentFilter("android.intent.action.BATTERY_CHANGED"))
    }

    override fun stopImpl() {
        context.unregisterReceiver(receiver)
    }
}