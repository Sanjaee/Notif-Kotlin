package com.example.myapplication.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.fcm.ChatNotificationHelper
import com.example.myapplication.fcm.FcmTokenManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Service FCM: menangani token refresh dan pesan masuk.
 * Notifikasi akan tetap muncul walaupun app ditutup (killed/background).
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(FCM_TAG, "Refreshed token: $token")
        // Kirim token ke app server untuk mengirim notif ke device ini
        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(FCM_TAG, "From: ${remoteMessage.from}")

        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            Log.d(FCM_TAG, "Message data payload: $data")
            val conversationId = data["conversation_id"]
            if (!conversationId.isNullOrBlank()) {
                // Notifikasi chat: tampilkan dengan aksi Balas & Tandai dibaca (seperti WhatsApp)
                val otherName = data["other_display_name"] ?: data["sender_name"] ?: "Someone"
                val title = data["title"] ?: remoteMessage.notification?.title ?: otherName
                val body = data["body"] ?: data["message"] ?: remoteMessage.notification?.body ?: ""
                ChatNotificationHelper.showChatNotification(
                    this,
                    conversationId,
                    otherName,
                    title,
                    body
                )
            } else {
                val title = data["title"] ?: remoteMessage.notification?.title ?: getString(R.string.app_name)
                val body = data["body"] ?: remoteMessage.notification?.body ?: ""
                showNotification(title, body, data)
            }
        } else {
            remoteMessage.notification?.let { notif ->
                Log.d(FCM_TAG, "Message Notification Body: ${notif.body}")
                showNotification(
                    notif.title ?: getString(R.string.app_name),
                    notif.body ?: "",
                    emptyMap()
                )
            }
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        FcmChannelSetup.createDefaultChannel(this)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, FcmChannelSetup.DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_BASE + (System.currentTimeMillis() % 1000).toInt(), notification)
    }

    /**
     * Simpan FCM token lokal + kirim ke backend agar notifikasi push muncul saat app closed (seperti WhatsApp).
     */
    private fun sendRegistrationToServer(token: String) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        scope.launch {
            try {
                FcmTokenManager.saveFcmToken(applicationContext, token)
                withContext(Dispatchers.IO) {
                    val repo = ChatRepository(applicationContext)
                    repo.registerFcmToken(token).onSuccess {
                        Log.d(FCM_TAG, "FCM token synced to server")
                    }.onFailure {
                        Log.d(FCM_TAG, "FCM token saved locally; sync to server when app opens: ${it.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(FCM_TAG, "sendRegistrationToServer error", e)
            }
        }
    }

    companion object {
        private const val FCM_TAG = "FCM"
        private const val NOTIFICATION_ID_BASE = 1000
    }
}
