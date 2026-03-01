package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Conversation(
    @SerializedName("id") val id: String,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class Message(
    @SerializedName("id") val id: String,
    @SerializedName("conversation_id") val conversationId: String,
    @SerializedName("sender_id") val senderId: String,
    @SerializedName("message") val message: String,
    @SerializedName("message_type") val messageType: String = "text",
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("sender") val sender: User? = null
)

data class ConversationWithMeta(
    @SerializedName("conversation") val conversation: Conversation,
    @SerializedName("other_member") val otherMember: User? = null,
    @SerializedName("last_message") val lastMessage: Message? = null,
    @SerializedName("unread_count") val unreadCount: Long = 0L
)
