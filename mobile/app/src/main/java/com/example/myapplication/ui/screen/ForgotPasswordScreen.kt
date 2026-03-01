package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.ForgotPasswordViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onResetSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToVerifyReset: (String) -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.isOtpSent) {
        if (uiState.isOtpSent) {
            onNavigateToVerifyReset(uiState.email)
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title
                Text(
                    text = "Forgot Password?",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Enter your email to receive OTP code for password reset",
                    fontSize = 16.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(bottom = 32.dp)
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
                
                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.filter { char -> char != '\n' } },
                    label = { 
                        Text(
                            "Email",
                            color = Color(0xFF6B7280)
                        ) 
                    },
                    placeholder = { 
                        Text(
                            "Enter your email",
                            color = Color(0xFF9CA3AF)
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = Color(0xFF6B7280)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Black,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedLabelColor = Black,
                        unfocusedLabelColor = Color(0xFF6B7280),
                        cursorColor = Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Send OTP Button
                Button(
                    onClick = { viewModel.sendResetOTP(email) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading && email.isNotBlank(),
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
                            "Send OTP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Back to Login Link
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
