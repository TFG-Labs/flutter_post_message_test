package com.example.chromecustomtabswebview

import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class MessageNotificationHandler {


    companion object {
        val CHANNEL_ID: String = "channel_id"

        @SuppressLint("MissingPermission")
        fun showNotificationWithMessage(context: Context?, message: String?) {
            val intent = Intent()
            intent.setAction(PostMessageBroadcastReceiver.POST_MESSAGE_ACTION)
            if(context != null) {
                val pendingIntent =
                    PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

                val builder: NotificationCompat.Builder =
                    NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_media_ff)
                        .setContentTitle("Received a message")
                        .setContentText("FUUUUUUCKKK HOW message")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .addAction(R.drawable.ic_notification_overlay, "Reply back", pendingIntent)
                        .setAutoCancel(true)

                NotificationManagerCompat.from(context).notify(1, builder.build())
            }
        }

        fun createNotificationChannelIfNeeded(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "PostMessage Demo"
                val descriptionText = "A channel to send post message demo notification"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance)
                channel.description = descriptionText

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}