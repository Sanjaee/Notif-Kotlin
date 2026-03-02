package com.example.myapplication.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.myapplication.MainActivity

/**
 * Helper untuk menampilkan notifikasi chat dengan aksi Balas dan Tandai dibaca (seperti WhatsApp).
 */
object ChatNotificationHelper {

    private const val CHANNEL_ID = "fcm_chat_channel"
    private const val CHANNEL_NAME = "Pesan Chat"
    private const val CHAT_NOTIFICATION_ID_BASE = 2000

    fun createChatChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikasi pesan chat masuk"
            enableVibration(true)
            setShowBadge(true)
        }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    /**
     * Tampilkan notifikasi chat dengan aksi Reply dan Mark as read.
     * Payload FCM yang didukung: conversation_id, other_display_name (nama pengirim), title/body atau message.
     */
    fun showChatNotification(
        context: Context,
        conversationId: String,
        otherDisplayName: String,
        title: String,
        body: String
    ) {
        createChatChannelIfNeeded(context)
        val notificationId = chatNotificationId(conversationId)

        // Tap notifikasi → buka app ke layar Chat
        val openChatIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(ChatNotificationReceiver.EXTRA_CONVERSATION_ID, conversationId)
            putExtra(ChatNotificationReceiver.EXTRA_OTHER_DISPLAY_NAME, otherDisplayName)
        }
        val openChatPending = PendingIntent.getActivity(
            context,
            conversationId.hashCode() and 0x7FFF,
            openChatIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Aksi: Balas (dengan RemoteInput)
        val remoteInput = RemoteInput.Builder(ChatNotificationReceiver.REMOTE_INPUT_REPLY_KEY)
            .setLabel("Ketik balasan…")
            .build()
        val replyIntent = Intent(context, ChatNotificationReceiver::class.java).apply {
            action = ChatNotificationReceiver.ACTION_REPLY_CHAT
            putExtra(ChatNotificationReceiver.EXTRA_CONVERSATION_ID, conversationId)
            putExtra(ChatNotificationReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        // PendingIntent untuk aksi dengan RemoteInput wajib mutable (Android 12+)
        val replyPending = PendingIntent.getBroadcast(
            context,
            (conversationId.hashCode() and 0x7FFF) + 1,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "Balas",
            replyPending
        ).addRemoteInput(remoteInput).build()

        // Aksi: Tandai dibaca
        val markReadIntent = Intent(context, ChatNotificationReceiver::class.java).apply {
            action = ChatNotificationReceiver.ACTION_MARK_READ_CHAT
            putExtra(ChatNotificationReceiver.EXTRA_CONVERSATION_ID, conversationId)
            putExtra(ChatNotificationReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val markReadPending = PendingIntent.getBroadcast(
            context,
            (conversationId.hashCode() and 0x7FFF) + 2,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val markReadAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_view,
            "Tandai dibaca",
            markReadPending
        ).build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.example.myapplication.R.drawable.ic_stat_ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(openChatPending)
            .addAction(replyAction)
            .addAction(markReadAction)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    fun chatNotificationId(conversationId: String): Int {
        return CHAT_NOTIFICATION_ID_BASE + (conversationId.hashCode() and 0x7FFF)
    }
}
