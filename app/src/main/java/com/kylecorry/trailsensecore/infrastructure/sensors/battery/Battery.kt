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
            val pct =
                (batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.toFloat()
                    ?: 0f)
            return if (pct <= 0f) {
                _percent
            } else {
                pct
            }
        }
    override val capacity: Float
        get() {
            val cap =
                (batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                    ?.toFloat() ?: 0f) * 0.001f
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
            return (batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                ?.toFloat()
                ?: 0f) * 0.001f
        }
    override val hasValidReading: Boolean
        get() = hasReading

    private val batteryManager: BatteryManager? by lazy { context.getSystemService() }

    private var hasReading = false
    private var _percent = 0f
    private var _charging = false
    private var _voltage = 0f
    private var _health = BatteryHealth.Unknown

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
            _percent = 100 * level / scale.toFloat()
            _charging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
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

    override fun startImpl() {
        context.registerReceiver(receiver, IntentFilter("android.intent.action.BATTERY_CHANGED"))
    }

    override fun stopImpl() {
        context.unregisterReceiver(receiver)
    }
}