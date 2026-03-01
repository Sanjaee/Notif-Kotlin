package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.TokenExpiredException
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.websocket.ChatWebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val isTokenExpired: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState
    
    init {
        loadUserData()
        viewModelScope.launch {
            repository.getAccessToken()?.let { token ->
                ChatWebSocketManager.connect(token)
            }
        }
    }
    
    fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                errorMessage = null,
                isTokenExpired = false
            )
            
            repository.getCurrentUser().fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        errorMessage = null,
                        isTokenExpired = false
                    )
                },
                onFailure = { error ->
                    // Check if it's TokenExpiredException
                    if (error is TokenExpiredException) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null, // Don't show error message
                            isTokenExpired = true // Flag untuk redirect ke login
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load user data",
                            isTokenExpired = false
                        )
                    }
                }
            )
        }
    }
}

