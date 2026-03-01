package com.example.myapplication.ui.screen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Message
import com.example.myapplication.data.model.TokenExpiredException
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.websocket.ChatWebSocketManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val isLoading: Boolean = false,
    val messages: List<Message> = emptyList(),
    val currentUserId: String? = null,
    val errorMessage: String? = null,
    val sendError: String? = null,
    val isTokenExpired: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application)
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    private var newMessageJob: Job? = null

    fun loadMessages(conversationId: String) {
        newMessageJob?.cancel()
        viewModelScope.launch {
            val uid = repository.getCurrentUserId()
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, currentUserId = uid, isTokenExpired = false)
            // Connect WebSocket first so real-time messages are received
            repository.getAccessToken()?.let { token ->
                ChatWebSocketManager.connect(token)
            }
            // Start collecting new messages from WebSocket (for this conversation)
            newMessageJob = viewModelScope.launch {
                ChatWebSocketManager.newMessageFlow
                    .catch { }
                    .collect { message ->
                        if (message.conversationId == conversationId) {
                            val current = _uiState.value.messages
                            if (current.none { it.id == message.id }) {
                                _uiState.value = _uiState.value.copy(messages = current + message)
                            }
                        }
                    }
            }
            repository.getMessages(conversationId).fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(isLoading = false, messages = list.reversed(), errorMessage = null)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = if (e is TokenExpiredException) null else (e.message ?: "Failed to load messages"),
                        isTokenExpired = e is TokenExpiredException
                    )
                }
            )
            if (!_uiState.value.isTokenExpired) {
                repository.markConversationRead(conversationId)
            }
        }
    }

    fun sendMessage(conversationId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(sendError = null)
            repository.sendMessage(conversationId, text.trim()).fold(
                onSuccess = { newMsg ->
                    _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + newMsg, sendError = null)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        sendError = if (e is TokenExpiredException) null else (e.message ?: "Failed to send"),
                        isTokenExpired = e is TokenExpiredException
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    otherDisplayName: String = "Chat",
    onBack: () -> Unit,
    onUnauthorized: () -> Unit = {},
    viewModel: ChatViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val wsConnected by ChatWebSocketManager.connectionState.collectAsState(initial = false)
    var showWsDisconnectedPopup by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val currentUserId = uiState.currentUserId

    LaunchedEffect(wsConnected) {
        if (!wsConnected) showWsDisconnectedPopup = true
    }
    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
    }
    LaunchedEffect(uiState.isTokenExpired) {
        if (uiState.isTokenExpired) onUnauthorized()
    }
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    if (showWsDisconnectedPopup) {
        AlertDialog(
            onDismissRequest = { showWsDisconnectedPopup = false },
            title = { Text("Koneksi Chat", color = Black, fontWeight = FontWeight.Medium) },
            text = {
                Text(
                    "WebSocket tidak terhubung. Periksa koneksi internet Anda.",
                    color = Color(0xFF374151)
                )
            },
            confirmButton = {
                TextButton(onClick = { showWsDisconnectedPopup = false }) {
                    Text("OK", color = Black, fontWeight = FontWeight.Medium)
                }
            }
        )
    }

    Scaffold(
        containerColor = White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = otherDisplayName,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Black
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Black
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Black)
                    }
                }
                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = uiState.messages,
                            key = { it.id }
                        ) { msg ->
                            MessageBubble(
                                message = msg,
                                isFromMe = currentUserId == msg.senderId
                            )
                        }
                    }
                }
            }

            uiState.sendError?.let { err ->
                Text(
                    text = err,
                    color = Color(0xFFDC2626),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message") },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.sendMessage(conversationId, inputText)
                        inputText = ""
                    }
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Black
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isFromMe: Boolean
) {
    val backgroundColor = if (isFromMe) Color(0xFF3B82F6) else Color(0xFFE5E7EB)
    val textColor = if (isFromMe) White else Black

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.message,
                    color = textColor,
                    fontSize = 15.sp
                )
                Text(
                    text = message.createdAt.take(16).replace("T", " "),
                    color = if (isFromMe) Color.White.copy(alpha = 0.8f) else Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}
