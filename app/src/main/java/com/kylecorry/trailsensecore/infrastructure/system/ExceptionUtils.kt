package com.kylecorry.trailsensecore.infrastructure.system

import android.content.Context
import android.os.Build
import android.os.Looper
import com.kylecorry.andromeda.core.system.*
import com.kylecorry.trailsensecore.infrastructure.sensors.SensorDetailProvider
import java.time.Duration
import kotlin.concurrent.thread
import kotlin.system.exitProcess

object ExceptionUtils {

    fun report(context: Context, throwable: Throwable?, email: String, appName: String) {
        val androidVersion = Build.VERSION.SDK_INT
        val device = "${Android.fullDeviceName} (${Android.model})"
        val appVersion = Package.getVersionName(context)
        val message = throwable?.message ?: ""
        val stackTrace = throwable?.stackTraceToString() ?: ""
        var sensors = ""
        try {
            sensors = SensorDetailProvider().getSensorDetails(context)
        } catch (e: Exception) {
            // Don't do anything
        }

        val body =
            "Version: ${appVersion}\nDevice: ${device}\nAndroid SDK: ${androidVersion}\nSensors\n$sensors\n\nMessage: ${message}\n\n$stackTrace"

        val intent = Intents.email(
            email,
            "Error in $appName $appVersion",
            body
        )
        context.startActivity(intent)
    }

}