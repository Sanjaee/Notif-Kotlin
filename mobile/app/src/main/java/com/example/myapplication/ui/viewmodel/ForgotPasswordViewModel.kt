package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ForgotPasswordRequest
import com.example.myapplication.data.model.VerifyResetPasswordRequest
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOtpSent: Boolean = false,
    val isOtpVerified: Boolean = false,
    val isResetSuccess: Boolean = false,
    val email: String = "",
    val verifiedOtpCode: String = ""
)

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)
    
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState
    
    fun sendResetOTP(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Trim whitespace and newline characters
            val trimmedEmail = email.trim()
            
            val request = ForgotPasswordRequest(email = trimmedEmail)
            repository.forgotPassword(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isOtpSent = true,
                        email = trimmedEmail,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to send reset password OTP"
                    )
                }
            )
        }
    }
    
    // Verify OTP for reset password (step 1) - Client-side validation only
    // Actual OTP verification will be done together with password reset in step 2
    fun verifyResetOTP(email: String, otpCode: String) {
        // Trim whitespace and newline characters
        val trimmedEmail = email.trim()
        val trimmedOtpCode = otpCode.trim()
        
        // Client-side validation: Check if OTP is 6 digits
        if (trimmedOtpCode.length != 6 || !trimmedOtpCode.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isOtpVerified = false,
                errorMessage = "OTP must be 6 digits"
            )
            return
        }
        
        // OTP format is valid, mark as verified and proceed to password screen
        // Actual verification will happen in step 2 when submitting password
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isOtpVerified = true,
            verifiedOtpCode = trimmedOtpCode,
            email = trimmedEmail,
            errorMessage = null
        )
    }
    
    // Set new password after OTP verified (step 2)
    fun setNewPassword(email: String, otpCode: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Trim whitespace and newline characters
            val trimmedEmail = email.trim()
            val trimmedOtpCode = otpCode.trim()
            val trimmedNewPassword = newPassword.trim()
            
            val request = VerifyResetPasswordRequest(
                email = trimmedEmail,
                otpCode = trimmedOtpCode,
                newPassword = trimmedNewPassword
            )
            
            repository.verifyResetPassword(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isResetSuccess = true,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to reset password"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun resetSuccessState() {
        _uiState.value = _uiState.value.copy(isResetSuccess = false, isOtpSent = false)
    }
    
    fun resetOtpVerifiedState() {
        _uiState.value = _uiState.value.copy(isOtpVerified = false, verifiedOtpCode = "")
    }
}

