package com.kylecorry.trailsensecore.infrastructure.time

class Throttle(private val maxTimeMs: Long) {

    private var lastTime = 0L

    fun isThrottled(): Boolean {
        val throttled = System.currentTimeMillis() - lastTime < maxTimeMs
        if (!throttled) {
            lastTime = System.currentTimeMillis()
        }
        return throttled
    }

}