package com.kylecorry.trailsensecore.infrastructure.flashlight

import android.view.Window
import android.view.WindowManager

class ScreenFlashlight(private val window: Window): IFlashlight {
    override fun on() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        window.attributes = layoutParams
    }

    override fun off() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = layoutParams
    }
}