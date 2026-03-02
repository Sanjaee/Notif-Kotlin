package com.example.myapplication.data.repository

import android.content.Context
import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.data.api.CreateConversationRequest
import com.example.myapplication.data.api.FcmTokenRequest
import com.example.myapplication.data.api.SendMessageRequest
import com.example.myapplication.data.model.*
import com.example.myapplication.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.first
import retrofit2.Response

class ChatRepository(private val context: Context) {
    private val apiService = ApiClient.chatApiService
    private val preferencesManager = PreferencesManager(context)

    private suspend fun getToken(): String? = preferencesManager.accessToken.first()

    suspend fun getAccessToken(): String? = getToken()

    suspend fun getCurrentUserId(): String? = preferencesManager.userId.first()

    private fun parseError(response: Response<*>): String {
        val body = response.errorBody()?.string() ?: return "Request failed"
        return try {
            org.json.JSONObject(body).optString("message", body.take(200))
        } catch (_: Exception) {
            body.take(200)
        }
    }

    private suspend fun handleUnauthorized(): Result<Nothing> {
        preferencesManager.clearTokens()
        return Result.failure(TokenExpiredException("Not authenticated"))
    }

    suspend fun getUsers(limit: Int = 20, offset: Int = 0): Result<List<User>> {
        val token = getToken() ?: return handleUnauthorized()
        return try {
            val response = apiService.getUsers("Bearer $token", limit, offset)
            if (response.isSuccessful) {
                val data = response.body()?.data
                val list = data?.users ?: emptyList()
                Result.success(list)
            } else {
                if (response.code() == 401) handleUnauthorized()
                else Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            if (e is TokenExpiredException) Result.failure(e) else Result.failure(e)
        }
    }

    suspend fun getOrCreateConversation(otherUserId: String): Result<Conversation> {
        val token = getToken() ?: return handleUnauthorized()
        return try {
            val response = apiService.getOrCreateConversation(
                "Bearer $token",
                CreateConversationRequest(otherUserId)
            )
            if (response.isSuccessful) {
                val conv = response.body()?.data?.conversation
                    ?: return Result.failure(Exception("No conversation in response"))
                Result.success(conv)
            } else {
                if (response.code() == 401) handleUnauthorized()
                else Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            if (e is TokenExpiredException) Result.failure(e) else Result.failure(e)
        }
    }

    suspend fun getConversations(limit: Int = 20, offset: Int = 0): Result<List<ConversationWithMeta>> {
        val token = getToken() ?: return handleUnauthorized()
        return try {
            val response = apiService.getConversations("Bearer $token", limit, offset)
            if (response.isSuccessful) {
                val list = response.body()?.data?.conversations ?: emptyList()
                Result.success(list)
            } else {
                if (response.code() == 401) handleUnauthorized()
                else Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            if (e is TokenExpiredException) Result.failure(e) else Result.failure(e)
        }
    }

    suspend fun getMessages(conversationId: String, limit: Int = 50, offset: Int = 0): Result<List<Message>> {
        val token = getToken() ?: return handleUnauthorized()
        return try {
            val response = apiService.getMessages("Bearer $token", conversationId, limit, offset)
            if (response.isSuccessful) {
                val list = response.body()?.data?.messages ?: emptyList()
                Result.success(list)
            } else {
                if (response.code() == 401) handleUnauthorized()
                else Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            if (e is TokenExpiredException) Result.failure(e) else Result.failure(e)
        }
    }

    suspend fun sendMessage(conversationId: String, message: String, messageType: String = "text"): Result<Message> {
        val token = getToken() ?: return handleUnauthorized()
        return try {
            val response = apiService.sendMessage(
                "Bearer $token",
                conversationId,
                SendMessageRequest(message, messageType)
            )
            if (response.isSuccessful) {
                val msg = response.body()?.data?.message
                    ?: return Result.failure(Exception("No message in response"))
                Result.success(msg)
            } else {
                if (response.code() == 401) handleUnauthorized()
                else Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            if (e is TokenExpiredException) Result.failure(e) else Result.failure(e)
        }
    }

    suspend fun markConversationRead(conversationId: String): Result<Unit> {
        val token = getToken() ?: return handleUnauthorized()
        return try {
            val response = apiService.markConversationRead("Bearer $token", conversationId)
            if (response.isSuccessful) Result.success(Unit)
            else {
                if (response.code() == 401) handleUnauthorized()
                else Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            if (e is TokenExpiredException) Result.failure(e) else Result.failure(e)
        }
    }

    /** Daftarkan FCM token ke server agar notifikasi push muncul saat app closed (seperti WhatsApp). */
    suspend fun registerFcmToken(fcmToken: String): Result<Unit> {
        val token = getToken() ?: return Result.failure(Exception("Not logged in"))
        if (fcmToken.isBlank()) return Result.success(Unit)
        return try {
            val response = apiService.registerFcmToken("Bearer $token", FcmTokenRequest(fcmToken))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(parseError(response)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
