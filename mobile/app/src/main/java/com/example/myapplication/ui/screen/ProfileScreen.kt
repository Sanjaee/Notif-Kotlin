package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.ProfileCardComponent
import com.example.myapplication.ui.components.ProfileImageComponent
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.HomeViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory
import android.app.Application
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }

    Scaffold(
        containerColor = White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Black
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Black
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Black)
                    }
                }
                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            uiState.errorMessage!!,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
                uiState.user != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProfileImageComponent(
                            profilePhotoUrl = uiState.user?.profilePhoto,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
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
                        ProfileCardComponent(user = uiState.user!!)
                        Spacer(modifier = Modifier.height(24.dp))
                        TextButton(onClick = onLogout) {
                            Text(
                                "Logout",
                                color = Black,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
