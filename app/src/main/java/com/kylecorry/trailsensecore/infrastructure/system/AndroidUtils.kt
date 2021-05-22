package com.kylecorry.trailsensecore.infrastructure.system

import android.os.Build

object AndroidUtils {

    fun getSDK(): Int {
        return Build.VERSION.SDK_INT
    }

    fun getVersionName(): String {
        return Build.VERSION.RELEASE
    }

}