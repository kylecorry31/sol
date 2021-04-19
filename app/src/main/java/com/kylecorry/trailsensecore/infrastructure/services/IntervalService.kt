package com.kylecorry.trailsensecore.infrastructure.services

import android.content.Intent
import com.kylecorry.trailsensecore.infrastructure.time.Intervalometer
import java.time.Duration

abstract class IntervalService(val period: Duration, val tag: String): ForegroundService() {

    private val intervalometer = Intervalometer {
        doWork()
    }

    override fun onServiceStarted(intent: Intent?, flags: Int, startId: Int): Int {
        acquireWakelock(tag)
        intervalometer.interval(period)
        return START_STICKY
    }

    override fun onDestroy() {
        intervalometer.stop()
        super.onDestroy()
    }

    abstract fun doWork()
}