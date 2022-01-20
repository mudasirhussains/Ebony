package com.ebony.flirts.meet.black.women

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlin.random.Random
import android.os.Vibrator

import android.media.RingtoneManager

import android.media.Ringtone
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.window.SplashScreen
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject


class FirebaseService : FirebaseMessagingService() {
    private var mNotificationManager: NotificationManager? = null
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val body = remoteMessage.notification?.body.toString()
        val title = remoteMessage.notification?.title.toString()
        Log.d("FCMBody", "onMessageReceived: $title")


        callNotificationBuilder(body, title)

    }

    private fun callNotificationBuilder(body: String, title: String) {

// playing audio and vibration when user see request
        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(applicationContext, notification)
        r.play()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            r.isLooping = false
        }

        // vibration
        val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(100, 300, 300, 300)
        v.vibrate(pattern, -1)
        val builder = NotificationCompat.Builder(this, "CHANNEL_ID")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.ic_launcher_background)
        } else {
            builder.setSmallIcon(R.drawable.ic_launcher_background)
        }

        builder.setSound(notification)

        var pendingIntent: PendingIntent? = null


        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.putExtra("dashboardStatus", "noFilter")
        resultIntent.putExtra("tagActivity", "fcmActivity")
        resultIntent.putExtra("fcmKeyword", title)
        pendingIntent =
            PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)


        builder.setContentTitle(title)
//        builder.setContentTitle(remoteMessage.notification!!.sound)
        builder.setContentText(body)
        builder.setContentIntent(pendingIntent)
        builder.setStyle(
            NotificationCompat.BigTextStyle().bigText(
                body
            )
        )
        builder.setAutoCancel(true)
        builder.priority = Notification.PRIORITY_MAX
        mNotificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "Your_channel_id"
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                IMPORTANCE_HIGH
            )
            mNotificationManager!!.createNotificationChannel(channel)
            builder.setChannelId(channelId)
        }
// notificationId is a unique int for each notification that you must define
        mNotificationManager!!.notify(100, builder.build())
    }
}