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
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.VerifyEmailViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(
    token: String,
    onVerifySuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: VerifyEmailViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.verifyEmail(token)
    }
    
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
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Verifying email...",
                        fontSize = 16.sp,
                        color = Color(0xFF6B7280)
                    )
                } else if (uiState.isVerifySuccess) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFD1FAE5)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✓",
                                fontSize = 48.sp,
                                color = Color(0xFF059669),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "Email Verified!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Your email has been successfully verified.",
                                fontSize = 16.sp,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        }
                    }
                } else {
                    uiState.errorMessage?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFEE2E2)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "✗",
                                    fontSize = 48.sp,
                                    color = Color(0xFFDC2626),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Text(
                                    text = "Verification Failed",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = error,
                                    fontSize = 16.sp,
                                    color = Color(0xFF991B1B),
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Back to Login",
                        color = Black,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
