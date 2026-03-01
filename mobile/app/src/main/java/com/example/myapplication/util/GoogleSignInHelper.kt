package com.example.myapplication.util

import android.content.Context
import com.example.myapplication.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

object GoogleSignInHelper {
    fun getGoogleSignInClient(context: Context): GoogleSignInClient? {
        return try {
            val webClientId = context.getString(R.string.google_web_client_id)
            
            if (webClientId == "YOUR_WEB_CLIENT_ID_HERE" || webClientId.isEmpty()) {
                // Web Client ID belum dikonfigurasi, return null
                return null
            }
            
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
            
            GoogleSignIn.getClient(context, gso)
        } catch (e: Exception) {
            // Handle any exception gracefully
            null
        }
    }
    
    /**
     * Mendapatkan intent untuk sign in Google yang memaksa pemilihan akun.
     * Method ini akan sign out terlebih dahulu untuk memastikan account picker muncul.
     * 
     * @param onIntentReady Callback yang dipanggil dengan sign in intent setelah sign out selesai
     */
    fun getSignInIntentWithAccountPicker(
        context: Context,
        onIntentReady: (android.content.Intent?) -> Unit
    ) {
        try {
            val client = getGoogleSignInClient(context) ?: run {
                onIntentReady(null)
                return
            }
            
            // Sign out terlebih dahulu untuk memastikan account picker muncul
            // Ini tidak akan mempengaruhi aplikasi karena kita hanya ingin memilih akun
            // addOnCompleteListener akan dipanggil baik untuk success maupun failure
            client.signOut().addOnCompleteListener {
                // Setelah sign out selesai (baik success maupun failure), ambil sign in intent
                // Intent ini akan memaksa account picker muncul karena tidak ada akun yang tersimpan
                val signInIntent = client.signInIntent
                onIntentReady(signInIntent)
            }
        } catch (e: Exception) {
            onIntentReady(null)
        }
    }
    
    fun isGoogleSignInConfigured(context: Context): Boolean {
        return try {
            val webClientId = context.getString(R.string.google_web_client_id)
            webClientId != "YOUR_WEB_CLIENT_ID_HERE" && webClientId.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    fun handleSignInResult(data: android.content.Intent?): GoogleSignInAccount? {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            null
        }
    }
}
