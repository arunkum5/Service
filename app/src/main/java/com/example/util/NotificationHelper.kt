package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

data class CustomerNotification(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // STATUS_UPDATE, PHOTO_UPLOAD, COMPONENT_HEALTH, REMINDER
    val vehicleNumber: String,
    val photoUrl: String? = null
)

object NotificationHelper {

    private const val CHANNEL_SERVICE_PROGRESS = "gvd_service_progress"
    private const val CHANNEL_REMINDERS = "gvd_service_reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val progressChannel = NotificationChannel(
                CHANNEL_SERVICE_PROGRESS,
                "Service Progress & Photo Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time updates on vehicle inspection, photos, and technician notes"
                enableVibration(true)
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Service Reminders & Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Automated reminders for maintenance, PPF, and insurance"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(progressChannel)
            manager?.createNotificationChannel(reminderChannel)
        }
    }

    fun postServiceUpdateNotification(
        context: Context,
        title: String,
        message: String,
        vehicleNo: String
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_SERVICE_PROGRESS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = (System.currentTimeMillis() % 100000).toInt()
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Android 13+ permission not granted yet
        }
    }
}
