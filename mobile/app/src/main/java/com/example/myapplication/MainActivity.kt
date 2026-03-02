package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.websocket.ChatWebSocketManager
import com.example.myapplication.navigation.NavGraph
import com.example.myapplication.navigation.Screen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.White
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val repository by lazy { AuthRepository(this) }

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted: Boolean -> optional handling */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()
        fetchFcmTokenIfNeeded()
        // Set theme untuk aplikasi setelah splash
        setTheme(com.example.myapplication.R.style.Theme_MyApplication)
        
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            MyApplicationTheme {
                var startDestination by remember { mutableStateOf<String?>(null) }
                var showSplash by remember { mutableStateOf(true) }
                
                // Splash screen dengan logo
                if (showSplash) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.example.myapplication.R.drawable.logo),
                            contentDescription = "zacode logo",
                            modifier = Modifier
                                .size(150.dp)
                                .padding(16.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                }
                
                LaunchedEffect(Unit) {
                    // Tampilkan splash screen selama 2 detik
                    delay(2000)
                    showSplash = false
                    
                    // Check login state with error handling
                    try {
                        val loggedIn = repository.isLoggedIn()
                        startDestination = if (loggedIn) Screen.Home.route else Screen.Login.route
                    } catch (e: Exception) {
                        // Jika ada error, default ke Login screen
                        startDestination = Screen.Login.route
                    }
                }
                
                // Show loading or nothing until we determine start destination
                if (!showSplash) {
                    val pendingChatConvId = remember { intent.getStringExtra(com.example.myapplication.fcm.ChatNotificationReceiver.EXTRA_CONVERSATION_ID) }
                    val pendingChatOtherName = remember { intent.getStringExtra(com.example.myapplication.fcm.ChatNotificationReceiver.EXTRA_OTHER_DISPLAY_NAME) }
                    startDestination?.let { destination ->
                        NavGraph(
                            startDestination = destination,
                            onLogout = {
                                lifecycleScope.launch {
                                    ChatWebSocketManager.disconnect()
                                    repository.logout()
                                }
                            },
                            pendingChatConversationId = pendingChatConvId,
                            pendingChatOtherName = pendingChatOtherName
                        )
                    } ?: run {
                        // Show loading indicator while checking login state
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(White),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    /**
     * Minta izin notifikasi (Android 13+). Wajib agar FCM bisa menampilkan notifikasi.
     * Lihat: https://firebase.google.com/docs/cloud-messaging/android/client#request-permission
     */
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (dan app) bisa menampilkan notifikasi.
                return
            }
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Opsional: tampilkan UI penjelasan sebelum minta izin.
            }
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun fetchFcmTokenIfNeeded() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result ?: return@addOnCompleteListener
            if (token.isNotEmpty()) {
                lifecycleScope.launch {
                    com.example.myapplication.fcm.FcmTokenManager.saveFcmToken(this@MainActivity, token)
                    // Daftarkan ke server agar FCM muncul saat app closed (jika user sudah login)
                    ChatRepository(this@MainActivity).registerFcmToken(token)
                }
            }
        }
    }
}