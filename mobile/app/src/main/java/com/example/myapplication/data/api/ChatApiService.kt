package com.example.myapplication.data.api

import com.example.myapplication.data.model.ApiResponse
import com.example.myapplication.data.model.Conversation
import com.example.myapplication.data.model.ConversationWithMeta
import com.example.myapplication.data.model.Message
import com.example.myapplication.data.model.User
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface ChatApiService {
    @GET("api/v1/chat/users")
    suspend fun getUsers(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<ChatUsersData>>

    @POST("api/v1/chat/conversations")
    suspend fun getOrCreateConversation(
        @Header("Authorization") token: String,
        @Body body: CreateConversationRequest
    ): Response<ApiResponse<ConversationData>>

    @GET("api/v1/chat/conversations")
    suspend fun getConversations(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<ConversationsData>>

    @GET("api/v1/chat/conversations/{id}/messages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("id") conversationId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<MessagesData>>

    @POST("api/v1/chat/conversations/{id}/messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Path("id") conversationId: String,
        @Body body: SendMessageRequest
    ): Response<ApiResponse<MessageData>>

    @POST("api/v1/chat/conversations/{id}/read")
    suspend fun markConversationRead(
        @Header("Authorization") token: String,
        @Path("id") conversationId: String
    ): Response<ApiResponse<Nothing>>

    /** Daftarkan FCM token agar server kirim push notifikasi saat app closed (seperti WhatsApp). */
    @POST("api/v1/chat/fcm-token")
    suspend fun registerFcmToken(
        @Header("Authorization") authToken: String,
        @Body body: FcmTokenRequest
    ): Response<ApiResponse<Nothing>>
}

data class FcmTokenRequest(
    @SerializedName("fcm_token") val fcmToken: String
)

data class CreateConversationRequest(
    @SerializedName("other_user_id") val otherUserId: String
)

data class SendMessageRequest(
    @SerializedName("message") val message: String,
    @SerializedName("message_type") val messageType: String = "text"
)

data class ChatUsersData(
    @SerializedName("users") val users: List<User>
)

data class ConversationData(
    @SerializedName("conversation") val conversation: Conversation
)

data class ConversationsData(
    @SerializedName("conversations") val conversations: List<ConversationWithMeta>
)

data class MessagesData(
    @SerializedName("messages") val messages: List<Message>
)

data class MessageData(
    @SerializedName("message") val message: Message
)

