package com.kylecorry.trailsensecore.infrastructure.system

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

object NotificationUtils {

    val CHANNEL_IMPORTANCE_HIGH =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NotificationManager.IMPORTANCE_HIGH else 4
    val CHANNEL_IMPORTANCE_DEFAULT =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NotificationManager.IMPORTANCE_DEFAULT else 3
    val CHANNEL_IMPORTANCE_LOW =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NotificationManager.IMPORTANCE_LOW else 2

    fun isNotificationActive(context: Context, notificationId: Int): Boolean {
        val notificationManager = getNotificationManager(context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager?.activeNotifications?.any { it.id == notificationId } ?: false
        } else {
            // TODO: Determine if the notification exists
            false
        }
    }

    fun send(context: Context, notificationId: Int, notification: Notification) {
        val notificationManager = getNotificationManager(context)
        notificationManager?.notify(notificationId, notification)
    }

    fun builder(context: Context, channel: String): Notification.Builder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channel)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }
    }

    fun cancel(context: Context, notificationId: Int) {
        val notificationManager = getNotificationManager(context)
        notificationManager?.cancel(notificationId)
    }

    fun createChannel(
        context: Context,
        id: String,
        name: String,
        description: String,
        importance: Int,
        muteSound: Boolean = false
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = NotificationChannel(id, name, importance).apply {
            this.description = description
            if (muteSound) {
                setSound(null, null)
            }
        }
        getNotificationManager(context)?.createNotificationChannel(channel)
    }

    private fun getNotificationManager(context: Context): NotificationManager? {
        return context.getSystemService()
    }

}