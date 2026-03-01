package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.VerifyEmailRequest
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class VerifyEmailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isVerifySuccess: Boolean = false
)

class VerifyEmailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)
    
    private val _uiState = MutableStateFlow(VerifyEmailUiState())
    val uiState: StateFlow<VerifyEmailUiState> = _uiState
    
    fun verifyEmail(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val request = VerifyEmailRequest(token = token)
            repository.verifyEmail(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isVerifySuccess = true,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Email verification failed"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun resetSuccessState() {
        _uiState.value = _uiState.value.copy(isVerifySuccess = false)
    }
}

