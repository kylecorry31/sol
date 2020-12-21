package com.kylecorry.trailsensecore.infrastructure.vibration

import android.Manifest
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import java.time.Duration

class Vibrator(context: Context) {

    private val vibrator by lazy { context.getSystemService<Vibrator>() }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrate(duration: Duration) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(LongArray(3) {
                when (it) {
                    0 -> 0
                    else -> duration.toMillis()
                }
            }, 0))
        } else {
            vibrator?.vibrate(LongArray(3) {
                when (it) {
                    0 -> 0
                    else -> duration.toMillis()
                }
            }, 0)
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun stop() {
        vibrator?.cancel()
    }


}