package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import com.example.myapplication.ui.viewmodel.RegisterViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToVerifyOTP: (String) -> Unit,
    viewModel: RegisterViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.isRegisterSuccess) {
        if (uiState.isRegisterSuccess) {
            onNavigateToVerifyOTP(uiState.email)
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
                    text = "Create Account",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Sign up to get started",
                    fontSize = 16.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
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
                
                // Full Name Field
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it.filter { char -> char != '\n' } },
                    label = { 
                        Text(
                            "Full Name",
                            color = Color(0xFF6B7280)
                        ) 
                    },
                    placeholder = { 
                        Text(
                            "Enter your full name",
                            color = Color(0xFF9CA3AF)
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Full Name",
                            tint = Color(0xFF6B7280)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Black,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedLabelColor = Black,
                        unfocusedLabelColor = Color(0xFF6B7280),
                        cursorColor = Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
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
                    modifier = Modifier.fillMaxWidth(),
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
                
                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.filter { char -> char != '\n' } },
                    label = { 
                        Text(
                            "Password",
                            color = Color(0xFF6B7280)
                        ) 
                    },
                    placeholder = { 
                        Text(
                            "Enter your password",
                            color = Color(0xFF9CA3AF)
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
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
                        focusedBorderColor = Black,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
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
                            "Confirm Password",
                            color = Color(0xFF6B7280)
                        ) 
                    },
                    placeholder = { 
                        Text(
                            "Confirm your password",
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
                        focusedBorderColor = if (password.isNotBlank() && password != confirmPassword) Color(0xFFDC2626) else Black,
                        unfocusedBorderColor = if (password.isNotBlank() && password != confirmPassword) Color(0xFFDC2626) else Color(0xFFE5E7EB),
                        focusedLabelColor = Black,
                        unfocusedLabelColor = Color(0xFF6B7280),
                        cursorColor = Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Password mismatch error
                if (password.isNotBlank() && password != confirmPassword) {
                    Text(
                        text = "Passwords do not match",
                        color = Color(0xFFDC2626),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Register Button
                Button(
                    onClick = {
                        if (password == confirmPassword) {
                            viewModel.register(
                                fullName = fullName,
                                email = email,
                                password = password
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading && fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && password == confirmPassword,
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
                            "Register",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Login Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                    TextButton(
                        onClick = onNavigateToLogin,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Login",
                            color = Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
