package com.kylecorry.trailsensecore.infrastructure.system

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

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

    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

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

        val message = TextView(activity.applicationContext)
        message.text = rationale.reason
        message.movementMethod = LinkMovementMethod.getInstance()

        UiUtils.alertViewWithCancel(
            activity,
            rationale.title,
            message,
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