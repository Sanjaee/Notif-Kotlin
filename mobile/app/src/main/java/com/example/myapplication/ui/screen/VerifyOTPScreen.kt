package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.OtpInputField
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.VerifyOTPViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyOTPScreen(
    email: String,
    onVerifySuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: VerifyOTPViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    var otpCode by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.isVerifySuccess) {
        if (uiState.isVerifySuccess) {
            onVerifySuccess()
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
                    text = "Enter OTP Code",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Enter the OTP sent to:",
                    fontSize = 16.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = email,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Black,
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
                
                // OTP Input
                OtpInputField(
                    otpText = otpCode,
                    onOtpTextChange = { 
                        if (it.length <= 6) {
                            otpCode = it.filter { char -> char.isDigit() }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    enabled = !uiState.isLoading
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Verify Button
                Button(
                    onClick = { viewModel.verifyOTP(email, otpCode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading && otpCode.length == 6,
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
                            "Verify OTP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Resend OTP
                TextButton(
                    onClick = { viewModel.resendOTP(email) },
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        "Resend OTP",
                        color = Black,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Back to Login
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        "Back to Login",
                        color = Black,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Success Message
                if (uiState.isResendSuccess) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFD1FAE5)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "OTP sent successfully!",
                            color = Color(0xFF059669),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
