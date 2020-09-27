package com.kylecorry.trailsensecore.infrastructure.system

import android.content.Context
import android.content.pm.PackageManager

object PackageUtils {

    fun getPackageName(context: Context): String {
        return context.packageName
    }

    fun getVersionName(context: Context): String {
        val packageManager = context.packageManager
        return packageManager.getPackageInfo(getPackageName(context), 0).versionName
    }

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

}