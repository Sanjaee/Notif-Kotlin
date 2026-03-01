package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.ForgotPasswordViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetNewPasswordScreen(
    email: String,
    otpCode: String,
    onResetSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.isResetSuccess) {
        if (uiState.isResetSuccess) {
            onResetSuccess()
            viewModel.resetSuccessState()
        }
    }
    
    Scaffold(
        containerColor = White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Create New Password",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Please enter your new password",
                    fontSize = 16.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Error Message
                uiState.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFEE2E2)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFDC2626),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                
                // New Password Field
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it.filter { char -> char != '\n' } },
                    label = { 
                        Text(
                            "New Password",
                            color = Color(0xFF6B7280)
                        ) 
                    },
                    placeholder = { 
                        Text(
                            "Minimum 8 characters",
                            color = Color(0xFF9CA3AF)
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "New Password",
                            tint = Color(0xFF6B7280)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color(0xFF6B7280)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (newPassword.isNotBlank() && newPassword.length < 8) Color(0xFFDC2626) else Black,
                        unfocusedBorderColor = if (newPassword.isNotBlank() && newPassword.length < 8) Color(0xFFDC2626) else Color(0xFFE5E7EB),
                        focusedLabelColor = Black,
                        unfocusedLabelColor = Color(0xFF6B7280),
                        cursorColor = Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Confirm Password Field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it.filter { char -> char != '\n' } },
                    label = { 
                        Text(
                            "Confirm New Password",
                            color = Color(0xFF6B7280)
                        ) 
                    },
                    placeholder = { 
                        Text(
                            "Re-enter your password",
                            color = Color(0xFF9CA3AF)
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Confirm Password",
                            tint = Color(0xFF6B7280)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                tint = Color(0xFF6B7280)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (confirmPassword.isNotBlank() && newPassword != confirmPassword) Color(0xFFDC2626) else Black,
                        unfocusedBorderColor = if (confirmPassword.isNotBlank() && newPassword != confirmPassword) Color(0xFFDC2626) else Color(0xFFE5E7EB),
                        focusedLabelColor = Black,
                        unfocusedLabelColor = Color(0xFF6B7280),
                        cursorColor = Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Validation Messages
                if (newPassword.isNotBlank() && newPassword.length < 8) {
                    Text(
                        text = "Password must be at least 8 characters",
                        color = Color(0xFFDC2626),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                if (confirmPassword.isNotBlank() && newPassword != confirmPassword) {
                    Text(
                        text = "Passwords do not match",
                        color = Color(0xFFDC2626),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Reset Password Button
                Button(
                    onClick = {
                        if (newPassword == confirmPassword && newPassword.length >= 8) {
                            viewModel.setNewPassword(email, otpCode, newPassword)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading && 
                             newPassword.isNotBlank() && 
                             newPassword.length >= 8 &&
                             newPassword == confirmPassword,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Black,
                        contentColor = White,
                        disabledContainerColor = Color(0xFFE5E7EB),
                        disabledContentColor = Color(0xFF9CA3AF)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Reset Password",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Back to Login
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        "Back to Login",
                        color = Black,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
