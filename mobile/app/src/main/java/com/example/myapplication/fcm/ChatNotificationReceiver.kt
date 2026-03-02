package com.example.myapplication.fcm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.example.myapplication.data.repository.ChatRepository
import kotlinx.coroutines.runBlocking

/**
 * Menangani aksi dari notifikasi chat: Balas (Reply) dan Tandai dibaca (Mark read).
 */
class ChatNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID) ?: return
        val action = intent.action ?: return

        when (action) {
            ACTION_REPLY_CHAT -> {
                val replyText = RemoteInput.getResultsFromIntent(intent)
                    ?.getCharSequence(REMOTE_INPUT_REPLY_KEY)?.toString()?.trim()
                if (!replyText.isNullOrEmpty()) {
                    runBlocking {
                        val repo = ChatRepository(context)
                        repo.sendMessage(conversationId, replyText).fold(
                            onSuccess = {
                                Log.d(TAG, "Reply sent for conversation $conversationId")
                            },
                            onFailure = { e ->
                                Log.e(TAG, "Reply failed", e)
                            }
                        )
                    }
                }
            }
            ACTION_MARK_READ_CHAT -> {
                runBlocking {
                    val repo = ChatRepository(context)
                    repo.markConversationRead(conversationId).fold(
                        onSuccess = {
                            Log.d(TAG, "Conversation $conversationId marked as read")
                        },
                        onFailure = { e ->
                            Log.e(TAG, "Mark read failed", e)
                        }
                    )
                }
            }
        }

        // Hapus notifikasi setelah aksi
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        if (notificationId != 0) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }
    }

    companion object {
        private const val TAG = "ChatNotifReceiver"
        const val ACTION_REPLY_CHAT = "com.example.myapplication.fcm.ACTION_REPLY_CHAT"
        const val ACTION_MARK_READ_CHAT = "com.example.myapplication.fcm.ACTION_MARK_READ_CHAT"
        const val EXTRA_CONVERSATION_ID = "conversation_id"
        const val EXTRA_OTHER_DISPLAY_NAME = "other_display_name"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val REMOTE_INPUT_REPLY_KEY = "reply_text"
    }
}
