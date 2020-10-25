package com.kylecorry.trailsensecore.infrastructure.system

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.kylecorry.trailsensecore.domain.geo.Coordinate

object IntentUtils {

    fun localIntent(context: Context, action: String): Intent {
        val i = Intent(action)
        i.`package` = context.packageName
        i.addCategory(Intent.CATEGORY_DEFAULT)
        return i
    }

    fun startService(context: Context, intent: Intent, foreground: Boolean = false) {
        if (foreground && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun pendingIntentExists(context: Context, requestCode: Int, intent: Intent): Boolean {
        return PendingIntent.getBroadcast(
            context, requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE
        ) != null
    }

    fun email(to: String, subject: String, body: String = ""): Intent {
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
    }

    fun url(url: String): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
    }

    fun geo(location: Coordinate): Intent {
        return url("geo:${location.latitude},${location.longitude}")
    }

    fun appSettings(context: Context): Intent {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", PackageUtils.getPackageName(context), null)
        intent.data = uri
        return intent
    }

}