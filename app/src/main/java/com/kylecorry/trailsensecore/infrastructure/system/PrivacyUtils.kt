package com.kylecorry.trailsensecore.infrastructure.system

import android.app.Activity
import android.view.Window
import android.view.WindowManager

object PrivacyUtils {

    fun setAllowScreenshots(activity: Activity, allowed: Boolean){
        setAllowScreenshots(activity.window, allowed)
    }

    fun setAllowScreenshots(window: Window, allowed: Boolean) {
        if (allowed) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

}