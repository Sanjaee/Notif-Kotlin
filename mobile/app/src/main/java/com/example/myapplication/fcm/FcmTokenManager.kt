package com.example.myapplication.fcm

import android.content.Context
import com.example.myapplication.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.first

/**
 * Helper untuk menyimpan dan mengambil FCM token (untuk dikirim ke backend atau ditampilkan di UI).
 */
object FcmTokenManager {

    suspend fun saveFcmToken(context: Context, token: String) {
        PreferencesManager(context).saveFcmToken(token)
    }

    suspend fun getFcmToken(context: Context): String? {
        return PreferencesManager(context).fcmToken.first()
    }
}
