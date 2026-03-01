package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.LoginViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory
import com.example.myapplication.util.GoogleSignInHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToVerifyOTP: (String) -> Unit,
    viewModel: LoginViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val context = LocalContext.current
    val repository = remember { AuthRepository(context) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Google Sign In - hanya inisialisasi jika sudah dikonfigurasi
    val isGoogleSignInConfigured = remember { GoogleSignInHelper.isGoogleSignInConfigured(context) }
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val account = GoogleSignInHelper.handleSignInResult(result.data)
        account?.let {
            viewModel.signInWithGoogle(it)
        } ?: run {
            // Handle error if needed
        }
    }
    
    // Check if user is already logged in and redirect to Home
    LaunchedEffect(Unit) {
        try {
            val loggedIn = repository.isLoggedIn()
            if (loggedIn) {
                onLoginSuccess()
            }
        } catch (e: Exception) {
            // Handle error gracefully, continue to show login screen
        }
    }
    
    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onLoginSuccess()
            viewModel.resetSuccessState()
        }
    }
    
    LaunchedEffect(uiState.requiresVerification) {
        if (uiState.requiresVerification && uiState.email.isNotBlank()) {
            // Tampilkan snackbar seperti toast di TSX
            snackbarHostState.showSnackbar(
                message = "📧 Email Belum Diverifikasi\nOTP telah dikirim ke email Anda. Silakan verifikasi email untuk melanjutkan.",
                duration = SnackbarDuration.Short
            )
            // Redirect ke verify OTP setelah snackbar ditampilkan
            kotlinx.coroutines.delay(1500) // Delay untuk menampilkan snackbar
            onNavigateToVerifyOTP(uiState.email)
        }
    }
    
    Scaffold(
        containerColor = White,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
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
                // Logo
                Image(
                    painter = painterResource(id = com.example.myapplication.R.drawable.logo),
                    contentDescription = "zacode logo",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 32.dp)
                )
                
                // Title
                Text(
                    text = "Welcome Back",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Sign in to your account",
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
                        .padding(bottom = 16.dp),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
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
                
                // Forgot Password Link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot Password?",
                        color = Black,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = onNavigateToForgotPassword
                            )
                    )
                }
                
                // Sign In Button
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
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
                            "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Google Sign In hanya ditampilkan jika sudah dikonfigurasi
                if (isGoogleSignInConfigured) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Divider with "OR"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE5E7EB),
                            thickness = 1.dp
                        )
                        Text(
                            text = "OR",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE5E7EB),
                            thickness = 1.dp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Google Sign In Button
                    OutlinedButton(
                        onClick = {
                            // Memanggil method yang akan sign out terlebih dahulu
                            // untuk memastikan account picker muncul setiap kali
                            GoogleSignInHelper.getSignInIntentWithAccountPicker(context) { signInIntent ->
                                signInIntent?.let {
                                    googleSignInLauncher.launch(it)
                                }
                            }
                        },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Black,
                        disabledContentColor = Color(0xFF9CA3AF)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (!uiState.isLoading) Color(0xFFE5E7EB) else Color(0xFFE5E7EB)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Google Icon
                        Image(
                            painter = painterResource(id = com.example.myapplication.R.drawable.google),
                            contentDescription = "Google logo",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                        Text(
                            "Sign in with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Register Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                    TextButton(
                        onClick = onNavigateToRegister,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Register",
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
