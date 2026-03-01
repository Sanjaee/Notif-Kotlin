package com.example.myapplication.ui.screen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Conversation
import com.example.myapplication.data.model.TokenExpiredException
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.ui.components.ProfileImageComponent
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUserUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val errorMessage: String? = null,
    val isTokenExpired: Boolean = false
)

class SearchUserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application)
    private val _uiState = MutableStateFlow(SearchUserUiState())
    val uiState: StateFlow<SearchUserUiState> = _uiState.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            repository.getUsers().fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(isLoading = false, users = list, errorMessage = null, isTokenExpired = false)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = if (e is TokenExpiredException) null else (e.message ?: "Failed to load users"),
                        isTokenExpired = e is TokenExpiredException
                    )
                }
            )
        }
    }

    suspend fun getOrCreateConversation(otherUserId: String): Result<Conversation> {
        return repository.getOrCreateConversation(otherUserId).also { result ->
            result.onFailure { e ->
                if (e is TokenExpiredException) {
                    _uiState.value = _uiState.value.copy(isTokenExpired = true)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUserScreen(
    onBack: () -> Unit,
    onUserSelected: (conversationId: String, otherDisplayName: String) -> Unit,
    onUnauthorized: () -> Unit = {},
    viewModel: SearchUserViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }
    LaunchedEffect(uiState.isTokenExpired) {
        if (uiState.isTokenExpired) onUnauthorized()
    }

    Scaffold(
        containerColor = White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Search User",
                        fontWeight = FontWeight.Bold,
                        color = Black
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Black)
                    }
                }
                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                uiState.errorMessage!!,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(16.dp)
                            )
                            Button(onClick = { viewModel.loadUsers() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                uiState.users.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No users found",
                            color = Color(0xFF6B7280),
                            fontSize = 16.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(items = uiState.users, key = { it.id }) { user ->
                            UserListItem(
                                user = user,
                                onClick = {
                                    scope.launch {
                                        viewModel.getOrCreateConversation(user.id).onSuccess { conv ->
                                            val displayName = user.fullName.take(100).ifBlank { user.email }.ifBlank { "Chat" }
                                            onUserSelected(conv.id, displayName)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserListItem(
    user: User,
    onClick: () -> Unit
) {
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
                profilePhotoUrl = user.profilePhoto,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName,
                    fontWeight = FontWeight.SemiBold,
                    color = Black
                )
                Text(
                    text = user.email,
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp
                )
            }
        }
    }
}
