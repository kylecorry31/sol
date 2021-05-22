package com.kylecorry.trailsensecore.infrastructure.system

import android.view.Window
import android.view.WindowManager

object ScreenUtils {

    fun setKeepScreenOn(window: Window, keepOn: Boolean){
        if (keepOn){
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun setBrightness(window: Window, brightness: Float){
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightness.coerceIn(0f, 1f)
        window.attributes = layoutParams
    }

    fun resetBrightness(window: Window){
        val layoutParams = window.attributes
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = layoutParams
    }

    fun setAllowScreenshots(window: Window, allowed: Boolean) {
        if (allowed) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

}