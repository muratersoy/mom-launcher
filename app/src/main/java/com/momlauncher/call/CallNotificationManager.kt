package com.momlauncher.call

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.momlauncher.R

object CallNotificationManager {
    const val CHANNEL_ID = "ongoing_call_channel"
    private const val NOTIFICATION_ID = 9001

    fun showCallNotification(context: Context, callerName: String, callerNumber: String, isRinging: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val callActivityIntent = Intent(context, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            callActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (callerName.isNotBlank()) callerName else callerNumber.ifBlank {
            context.getString(R.string.unknown_caller)
        }
        val subText = if (isRinging) {
            context.getString(R.string.incoming_call_title)
        } else {
            context.getString(R.string.ongoing_call_notification_title)
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call)
            .setContentTitle(title)
            .setContentText(subText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    fun cancelCallNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(NOTIFICATION_ID)
    }
}
