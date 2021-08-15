package com.kylecorry.trailsensecore.infrastructure.system

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.text.method.LinkMovementMethod
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.kylecorry.andromeda.alerts.Alerts
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.andromeda.permissions.requestPermissions

object PermissionUtils {

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestPermissionsWithRationale(
        activity: Activity,
        permissions: List<String>,
        rationale: PermissionRationale,
        requestCode: Int,
        buttonGrant: String,
        buttonDeny: String
    ) {
        val notGrantedPermissions = permissions.filterNot { Permissions.hasPermission(activity, it) }
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

        Alerts.dialog(
            activity,
            rationale.title,
            contentView = layout,
            okText = buttonGrant,
            cancelText = buttonDeny
        ) { cancelled ->
            if (!cancelled) {
                activity.requestPermissions(notGrantedPermissions, requestCode)
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