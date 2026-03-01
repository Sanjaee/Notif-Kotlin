package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ResendOTPRequest
import com.example.myapplication.data.model.VerifyOTPRequest
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class VerifyOTPUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isVerifySuccess: Boolean = false,
    val isResendSuccess: Boolean = false
)

class VerifyOTPViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)
    
    private val _uiState = MutableStateFlow(VerifyOTPUiState())
    val uiState: StateFlow<VerifyOTPUiState> = _uiState
    
    fun verifyOTP(email: String, otpCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Trim whitespace and newline characters
            val trimmedEmail = email.trim()
            val trimmedOtpCode = otpCode.trim()
            
            val request = VerifyOTPRequest(email = trimmedEmail, otpCode = trimmedOtpCode)
            repository.verifyOTP(request).fold(
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
                        errorMessage = error.message ?: "OTP verification failed"
                    )
                }
            )
        }
    }
    
    fun resendOTP(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Trim whitespace and newline characters
            val trimmedEmail = email.trim()
            
            val request = ResendOTPRequest(email = trimmedEmail)
            repository.resendOTP(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isResendSuccess = true,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to resend OTP"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null, isResendSuccess = false)
    }
    
    fun resetSuccessState() {
        _uiState.value = _uiState.value.copy(isVerifySuccess = false)
    }
}

