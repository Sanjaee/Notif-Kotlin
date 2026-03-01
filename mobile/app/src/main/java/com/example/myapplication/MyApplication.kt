package com.example.myapplication

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase - handle gracefully if not configured
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("MyApplication", "Firebase initialized successfully")
            }
        } catch (e: Exception) {
            Log.e("MyApplication", "Failed to initialize Firebase: ${e.message}", e)
            // Continue without Firebase - app will still work for regular login
        }
    }
}
