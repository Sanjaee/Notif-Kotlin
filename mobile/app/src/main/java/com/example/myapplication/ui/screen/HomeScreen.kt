package com.example.myapplication.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.HomeViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle token expired - redirect to login immediately
    LaunchedEffect(uiState.isTokenExpired) {
        if (uiState.isTokenExpired) {
            // Redirect langsung ke login tanpa menampilkan error
            onLogout()
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
            // Don't render anything if token expired - will redirect via LaunchedEffect
            if (uiState.isTokenExpired) {
                // Show loading while redirecting
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Redirecting to login...",
                        color = Color(0xFF6B7280),
                        fontSize = 16.sp
                    )
                }
            } else if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Loading user data...",
                        color = Color(0xFF6B7280),
                        fontSize = 16.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logout Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onLogout) {
                            Text(
                                "Logout",
                                color = Black,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Profile Photo or Logo
                    ProfileImage(
                        profilePhotoUrl = uiState.user?.profilePhoto,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    uiState.user?.let { user ->
                        Text(
                            text = "Welcome!",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Your profile information",
                            fontSize = 16.sp,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "User Information",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Black
                                )
                                
                                HorizontalDivider(color = Color(0xFFE5E7EB))
                                
                                InfoRow("Email", user.email)
                                InfoRow("Full Name", user.fullName)
                                user.username?.let { InfoRow("Username", it) }
                                InfoRow("User Type", user.userType)
                                InfoRow("Status", if (user.isActive) "Active" else "Inactive")
                                InfoRow("Verified", if (user.isVerified) "Yes" else "No")
                            }
                        }
                    } ?: run {
                        // Only show error if not token expired (token expired will redirect via LaunchedEffect)
                        if (!uiState.isTokenExpired && uiState.errorMessage != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFEE2E2)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Failed to load user data",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    
                                    uiState.errorMessage?.let { error ->
                                        Text(
                                            text = error,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileImage(
    profilePhotoUrl: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Box dengan size square dan clip CircleShape untuk memastikan bentuk bulat sempurna
    Box(
        modifier = modifier
            .size(100.dp)  // Pastikan square size (width == height)
            .clip(CircleShape)  // Clip dengan CircleShape
    ) {
        if (!profilePhotoUrl.isNullOrBlank()) {
            // Tampilkan foto profil Google jika ada
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(profilePhotoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile photo",
                modifier = Modifier.fillMaxSize(),  // Isi penuh Box
                contentScale = ContentScale.Crop,  // Crop untuk memastikan gambar mengisi lingkaran sempurna
                placeholder = painterResource(id = com.example.myapplication.R.drawable.logo),
                error = painterResource(id = com.example.myapplication.R.drawable.logo)
            )
        } else {
            // Tampilkan logo jika tidak ada foto profil
            Image(
                painter = painterResource(id = com.example.myapplication.R.drawable.logo),
                contentDescription = "zacode logo",
                modifier = Modifier.fillMaxSize(),  // Isi penuh Box
                contentScale = ContentScale.Crop  // Crop juga untuk logo agar bulat sempurna
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Black,
            fontWeight = FontWeight.Normal
        )
    }
}
