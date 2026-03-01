package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.RegisterRequest
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegisterSuccess: Boolean = false,
    val email: String = ""
)

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState
    
    fun register(
        fullName: String,
        email: String,
        password: String,
        gender: String? = null,
        dateOfBirth: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Trim whitespace and newline characters
            val trimmedFullName = fullName.trim()
            val trimmedEmail = email.trim()
            val trimmedPassword = password.trim()
            
            val request = RegisterRequest(
                fullName = trimmedFullName,
                email = trimmedEmail,
                password = trimmedPassword,
                username = null, // Username tidak digunakan, menggunakan fullName
                phone = null, // Phone tidak digunakan
                gender = gender,
                dateOfBirth = dateOfBirth
            )
            
            repository.register(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegisterSuccess = true,
                        email = trimmedEmail,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Registration failed"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun resetSuccessState() {
        _uiState.value = _uiState.value.copy(isRegisterSuccess = false)
    }
}

