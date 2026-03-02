package com.example.myapplication.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Mendaftarkan semua channel notifikasi FCM saat app startup.
 * Agar notifikasi tetap masuk saat app di-close (killed), channel harus sudah ada.
 */
object FcmChannelSetup {

    const val DEFAULT_CHANNEL_ID = "fcm_default_channel"
    private const val DEFAULT_CHANNEL_NAME = "Notifikasi Umum"

    fun createAllChannels(context: Context) {
        createDefaultChannel(context)
        ChatNotificationHelper.createChatChannelIfNeeded(context)
    }

    fun createDefaultChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            DEFAULT_CHANNEL_ID,
            DEFAULT_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikasi dari aplikasi"
            enableVibration(true)
            setShowBadge(true)
        }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }
}
