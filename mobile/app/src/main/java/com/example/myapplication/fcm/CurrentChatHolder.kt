package com.example.myapplication.fcm

/**
 * Menyimpan conversationId layar Chat yang sedang ditampilkan.
 * Dipakai agar notifikasi chat tidak muncul saat user sudah ada di layar chat tersebut.
 */
object CurrentChatHolder {
    @Volatile
    var conversationId: String? = null
}
