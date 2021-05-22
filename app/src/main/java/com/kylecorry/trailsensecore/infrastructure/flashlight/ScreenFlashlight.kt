package com.kylecorry.trailsensecore.infrastructure.flashlight

import android.view.Window
import com.kylecorry.trailsensecore.infrastructure.system.ScreenUtils

class ScreenFlashlight(private val window: Window): IFlashlight {
    override fun on() {
        ScreenUtils.setBrightness(window, 1f)
    }

    override fun off() {
        ScreenUtils.resetBrightness(window)
    }
}