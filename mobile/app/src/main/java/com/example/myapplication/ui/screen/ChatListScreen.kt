package com.example.myapplication.ui.screen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.websocket.ChatWebSocketManager
import com.example.myapplication.data.model.ConversationWithMeta
import com.example.myapplication.data.model.Message
import com.example.myapplication.fcm.ChatNotificationHelper
import com.example.myapplication.fcm.CurrentChatHolder
import com.example.myapplication.data.model.TokenExpiredException
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.ui.components.ProfileImageComponent
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class ChatListUiState(
    val isLoading: Boolean = false,
    val conversations: List<ConversationWithMeta> = emptyList(),
    val errorMessage: String? = null,
    val isTokenExpired: Boolean = false
)

class ChatListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application)
    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
        // Connect WebSocket early so by the time user opens a chat it's already connected (helps on HP/real device)
        viewModelScope.launch {
            repository.getAccessToken()?.let { ChatWebSocketManager.connect(it) }
        }
        // Real-time: update list + tampilkan notifikasi chat saat ada pesan baru (kecuali sedang di layar chat itu)
        viewModelScope.launch {
            ChatWebSocketManager.newMessageFlow.collect { message ->
                refreshConversationsSilent()
                showChatNotificationIfNeeded(message)
            }
        }
        viewModelScope.launch {
            ChatWebSocketManager.newNotificationFlow.collect { refreshConversationsSilent() }
        }
    }

    /** Refresh daftar + unread count di background, tanpa loading spinner (untuk real-time / saat kembali dari chat). */
    fun refreshConversationsSilent() {
        viewModelScope.launch {
            repository.getConversations().fold(
                onSuccess = { list ->
                    val sorted = list.sortedByDescending { item: ConversationWithMeta ->
                        item.lastMessage?.createdAt ?: item.conversation.updatedAt ?: item.conversation.createdAt ?: ""
                    }
                    _uiState.value = _uiState.value.copy(conversations = sorted, errorMessage = null)
                },
                onFailure = { _ -> /* keep current list */ }
            )
        }
    }

    /** Tampilkan notifikasi chat ketika dapat new_message dari WebSocket (jika user tidak sedang di layar chat tersebut). */
    private fun showChatNotificationIfNeeded(message: Message) {
        if (message.conversationId == CurrentChatHolder.conversationId) return
        val senderName = message.sender?.fullName ?: "Someone"
        ChatNotificationHelper.showChatNotification(
            getApplication(),
            message.conversationId,
            senderName,
            senderName,
            message.message.ifBlank { "Pesan baru" }
        )
    }

    fun loadConversations() {
        viewModelScope.launch {
            val hasCachedList = _uiState.value.conversations.isNotEmpty()
            // Hanya tampilkan loading jika belum ada data (hindari blank putih saat kembali dari chat)
            if (!hasCachedList) {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            }
            repository.getConversations().fold(
                onSuccess = { list ->
                    val sorted = list.sortedByDescending { item: ConversationWithMeta ->
                        item.lastMessage?.createdAt ?: item.conversation.updatedAt ?: item.conversation.createdAt ?: ""
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, conversations = sorted, errorMessage = null)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = if (e is TokenExpiredException) null else (e.message ?: "Failed to load conversations"),
                        isTokenExpired = e is TokenExpiredException
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onOpenChat: (conversationId: String, otherDisplayName: String) -> Unit,
    onSearchUser: () -> Unit,
    onOpenProfile: () -> Unit,
    onUnauthorized: () -> Unit = {},
    viewModel: ChatListViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val state by viewModel.uiState.collectAsState()
    val wsConnected by ChatWebSocketManager.connectionState.collectAsState(initial = false)
    var showWsDisconnectedPopup by remember { mutableStateOf(false) }
    var hasConnectedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(state.isTokenExpired) {
        if (state.isTokenExpired) onUnauthorized()
    }
    // Saat kembali dari chat, refresh list diam-diam agar unread count & urutan tetap benar
    LaunchedEffect(Unit) {
        if (state.conversations.isNotEmpty()) viewModel.refreshConversationsSilent()
    }
    LaunchedEffect(wsConnected) {
        if (wsConnected) {
            hasConnectedOnce = true
        } else if (hasConnectedOnce) {
            showWsDisconnectedPopup = true
        }
    }

    if (showWsDisconnectedPopup) {
        AlertDialog(
            onDismissRequest = { showWsDisconnectedPopup = false },
            title = { Text("Koneksi Chat", color = Black) },
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
                        "Chats",
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Black
                ),
                actions = {
                    IconButton(onClick = onSearchUser) {
                        Icon(
                            Icons.Default.PersonSearch,
                            contentDescription = "Search user",
                            tint = Black
                        )
                    }
                    IconButton(onClick = onOpenProfile) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Black
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Black)
                    }
                }
                state.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.errorMessage!!,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(16.dp)
                            )
                            Button(onClick = { viewModel.loadConversations() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                state.conversations.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "No conversations yet",
                                color = Color(0xFF6B7280),
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tap the search icon to start a chat",
                                color = Color(0xFF9CA3AF),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = state.conversations,
                            key = { item -> item.conversation.id }
                        ) { item ->
                            val otherName = item.otherMember?.fullName?.take(100) ?: item.otherMember?.email ?: "Chat"
                            ChatListItem(
                                item = item,
                                onClick = { onOpenChat(item.conversation.id, otherName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(
    item: ConversationWithMeta,
    onClick: () -> Unit
) {
    val other = item.otherMember
    val name = other?.fullName?.takeIf { it.isNotBlank() } ?: other?.email ?: "Unknown"
    val lastMsg = item.lastMessage?.message?.take(50)?.let { s -> if (s.length == 50) "$s..." else s } ?: "No messages yet"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = White,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileImageComponent(
                profilePhotoUrl = other?.profilePhoto,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.SemiBold,
                    color = Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = lastMsg,
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (item.unreadCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF3B82F6)
                ) {
                    Text(
                        text = if (item.unreadCount > 99) "99+" else item.unreadCount.toString(),
                        color = White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
