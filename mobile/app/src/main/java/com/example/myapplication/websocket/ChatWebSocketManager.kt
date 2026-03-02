package com.example.myapplication.websocket

import android.util.Log
import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.data.model.Message
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

/**
 * WebSocket manager untuk real-time chat dan notifikasi (tanpa polling).
 * Koneksi ke backend GET /api/v1/ws/chat.
 * - Emit [Message] saat backend kirim {"type":"new_message","message":{...}}
 * - Emit notifikasi (message_id) saat backend kirim {"type":"new_notification","message_id":"..."}
 * Auto-reconnect saat koneksi putus.
 */
object ChatWebSocketManager {
    private const val TAG = "ChatWebSocket"
    private const val WS_PATH = "api/v1/ws/chat"
    private const val RECONNECT_DELAY_MS = 3000L
    private const val MAX_RECONNECT_ATTEMPTS = 10

    private var baseUrl: String = "http://147.182.195.95:5000/"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var currentToken: String? = null
    private var lastTokenForReconnect: String? = null
    private var reconnectAttempts = 0
    private var reconnectJob: Job? = null

    private val _newMessageFlow = MutableSharedFlow<Message>(replay = 0, extraBufferCapacity = 64)
    val newMessageFlow: SharedFlow<Message> = _newMessageFlow

    /** Dikirim saat backend mengirim new_notification (real-time notif tanpa polling). */
    private val _newNotificationFlow = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 64)
    val newNotificationFlow: SharedFlow<String> = _newNotificationFlow

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

    fun setBaseUrl(url: String) {
        baseUrl = url.trimEnd('/').plus("/")
    }

    private fun wsUrl(token: String): String {
        val wsScheme = when {
            baseUrl.startsWith("https://") -> "wss://"
            else -> "ws://"
        }
        val host = baseUrl.removePrefix("http://").removePrefix("https://").trimEnd('/')
        return "$wsScheme$host/$WS_PATH?token=${java.net.URLEncoder.encode(token, "UTF-8")}"
    }

    private fun scheduleReconnect() {
        val token = lastTokenForReconnect ?: return
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(RECONNECT_DELAY_MS)
            reconnectAttempts++
            connect(token)
        }
    }

    fun connect(token: String) {
        if (token.isBlank()) {
            Log.w(TAG, "connect: token blank, skip")
            return
        }
        Log.d(TAG, "connect: connecting to WebSocket...")
        setBaseUrl(ApiClient.getBaseUrl())
        lastTokenForReconnect = token
        if (currentToken == token && webSocket != null) return
        reconnectJob?.cancel()
        reconnectJob = null
        webSocket?.close(1000, null)
        webSocket = null
        currentToken = token
        reconnectAttempts = 0
        _connectionState.value = false
        val request = Request.Builder()
            .url(wsUrl(token))
            .build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                reconnectAttempts = 0
                Log.d(TAG, "onOpen: WebSocket terhubung")
                scope.launch { _connectionState.value = true }
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                reconnectAttempts = 0
                try {
                    val json = gson.fromJson(text, JsonObject::class.java)
                    val type = json.get("type")?.asString ?: return
                    when (type) {
                        "new_message" -> {
                            val msgObj = json.getAsJsonObject("message") ?: return
                            val message = gson.fromJson(msgObj, Message::class.java)
                            scope.launch { _newMessageFlow.emit(message) }
                        }
                        "new_notification" -> {
                            val messageId = json.get("message_id")?.asString ?: return
                            scope.launch { _newNotificationFlow.emit(messageId) }
                        }
                    }
                } catch (_: Exception) { }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w(TAG, "onFailure: ${t.message}, response=${response?.code}")
                this@ChatWebSocketManager.webSocket = null
                currentToken = null
                scope.launch { _connectionState.value = false }
                scheduleReconnect()
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "onClosed: code=$code reason=$reason")
                this@ChatWebSocketManager.webSocket = null
                currentToken = null
                scope.launch { _connectionState.value = false }
                scheduleReconnect()
            }
        }
        webSocket = client.newWebSocket(request, listener)
    }

    fun disconnect() {
        this@ChatWebSocketManager.reconnectJob?.cancel()
        this@ChatWebSocketManager.reconnectJob = null
        this@ChatWebSocketManager.lastTokenForReconnect = null
        this@ChatWebSocketManager.reconnectAttempts = MAX_RECONNECT_ATTEMPTS
        this@ChatWebSocketManager.webSocket?.close(1000, null)
        this@ChatWebSocketManager.webSocket = null
        this@ChatWebSocketManager.currentToken = null
        this@ChatWebSocketManager._connectionState.value = false
    }

    fun isConnected(): Boolean = this@ChatWebSocketManager.webSocket != null
}
