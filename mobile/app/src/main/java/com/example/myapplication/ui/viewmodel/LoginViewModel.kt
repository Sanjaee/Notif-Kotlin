package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.EmailVerificationRequiredException
import com.example.myapplication.data.model.GoogleOAuthRequest
import com.example.myapplication.data.model.LoginRequest
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.websocket.ChatWebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val requiresVerification: Boolean = false,
    val email: String = ""
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, requiresVerification = false)
            
            // Trim whitespace and newline characters
            val trimmedEmail = email.trim()
            val trimmedPassword = password.trim()
            
            val request = LoginRequest(email = trimmedEmail, password = trimmedPassword)
            repository.login(request).fold(
                onSuccess = { authResponse ->
                    ChatWebSocketManager.connect(authResponse.accessToken)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        errorMessage = null,
                        requiresVerification = false
                    )
                },
                onFailure = { error ->
                    // Check if it's EmailVerificationRequiredException
                    if (error is EmailVerificationRequiredException) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null, // Don't show error message, just redirect
                            requiresVerification = true,
                            email = error.userEmail
                        )
                    } else {
                        val errorMsg = error.message ?: "Login failed"
                        // Also check if error message contains verification keywords (backward compatibility)
                        val requiresVerification = errorMsg.contains("not verified", ignoreCase = true) ||
                                errorMsg.contains("requires verification", ignoreCase = true) ||
                                errorMsg.contains("belum diverifikasi", ignoreCase = true) ||
                                errorMsg.contains("email belum", ignoreCase = true)
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = if (requiresVerification) null else errorMsg, // Don't show error if requires verification
                            requiresVerification = requiresVerification,
                            email = if (requiresVerification) trimmedEmail else ""
                        )
                    }
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun resetSuccessState() {
        _uiState.value = _uiState.value.copy(isLoginSuccess = false)
    }
    
    fun signInWithGoogle(googleSignInAccount: GoogleSignInAccount) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Get Google ID token
                val idToken = googleSignInAccount.idToken
                if (idToken == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to get Google ID token"
                    )
                    return@launch
                }
                
                // Authenticate with Firebase
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val firebaseAuth = FirebaseAuth.getInstance()
                val firebaseUser = firebaseAuth.signInWithCredential(credential).await().user
                
                if (firebaseUser != null) {
                    // Create Google OAuth request
                    val request = GoogleOAuthRequest(
                        email = firebaseUser.email ?: "",
                        fullName = firebaseUser.displayName ?: "",
                        profilePhoto = firebaseUser.photoUrl?.toString(),
                        googleId = firebaseUser.uid
                    )
                    
                    // Send to backend API
                    repository.googleOAuth(request).fold(
                        onSuccess = { authResponse ->
                            ChatWebSocketManager.connect(authResponse.accessToken)
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoginSuccess = true,
                                errorMessage = null
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Google sign in failed"
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to authenticate with Google"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Google sign in failed"
                )
            }
        }
    }
}

