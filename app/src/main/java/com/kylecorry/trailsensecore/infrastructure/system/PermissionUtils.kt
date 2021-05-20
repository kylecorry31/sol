package com.kylecorry.trailsensecore.infrastructure.system

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.text.method.LinkMovementMethod
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

object PermissionUtils {

    fun isBackgroundLocationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            isLocationEnabled(context)
        } else {
            hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    fun isLocationEnabled(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun isCameraEnabled(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.CAMERA)
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return context.getSystemService<PowerManager>()
            ?.isIgnoringBatteryOptimizations(PackageUtils.getPackageName(context)) ?: false
    }

    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestPermissions(activity: Activity, permissions: List<String>, requestCode: Int) {
        val notGrantedPermissions = permissions.filterNot { hasPermission(activity, it) }
        if (notGrantedPermissions.isEmpty()) {
            activity.onRequestPermissionsResult(
                requestCode,
                permissions.toTypedArray(),
                intArrayOf(PackageManager.PERMISSION_GRANTED)
            )
            return
        }
        ActivityCompat.requestPermissions(
            activity,
            notGrantedPermissions.toTypedArray(),
            requestCode
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestPermissionsWithRationale(
        activity: Activity,
        permissions: List<String>,
        rationale: PermissionRationale,
        requestCode: Int,
        buttonGrant: String,
        buttonDeny: String
    ) {
        val notGrantedPermissions = permissions.filterNot { hasPermission(activity, it) }
        if (notGrantedPermissions.isEmpty()) {
            activity.onRequestPermissionsResult(
                requestCode,
                permissions.toTypedArray(),
                intArrayOf(PackageManager.PERMISSION_GRANTED)
            )
            return
        }

        val layout = FrameLayout(activity)
        layout.setPadding(64, 0, 64, 0)
        val message = TextView(activity)
        message.text = rationale.reason
        message.movementMethod = LinkMovementMethod.getInstance()
        layout.addView(message)

        UiUtils.alertViewWithCancel(
            activity,
            rationale.title,
            layout,
            buttonGrant,
            buttonDeny
        ) { cancelled ->
            if (!cancelled) {
                requestPermissions(activity, notGrantedPermissions, requestCode)
            } else {
                activity.onRequestPermissionsResult(
                    requestCode,
                    notGrantedPermissions.toTypedArray(),
                    intArrayOf(PackageManager.PERMISSION_DENIED)
                )
            }
        }
    }

}