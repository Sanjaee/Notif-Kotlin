# Langkah-langkah Setup FCM (Firebase Cloud Messaging)

Agar notifikasi muncul **walaupun app ditutup**, ikuti langkah berikut.

---

## 1. Connect app ke Firebase (jika belum)

- Buka [Firebase Console](https://console.firebase.google.com/).
- Pilih project atau buat project baru.
- **Add app** → pilih **Android**.
- Isi **Android package name**: `com.example.myapplication` (sesuai `applicationId` di `build.gradle.kts`).
- Download `google-services.json` dan letakkan di folder **`mobile/app/`** (sejajar dengan `build.gradle.kts`).
- Pastikan di **Project settings** status **"Connected"** untuk app Android.

---

## 2. Tambah FCM ke app

Di project ini FCM sudah ditambahkan lewat:

- **`build.gradle.kts` (app)**:  
  `implementation(platform(libs.firebase.bom))` dan `implementation(libs.firebase.messaging)`  
- **Plugin**: `alias(libs.plugins.google.services)`  
- **Root `build.gradle`**: plugin `com.google.gms.google-services` (biasanya di level project).

Tidak perlu langkah tambahan untuk “Add FCM to your app” selama dependency dan `google-services.json` sudah benar.

---

## 3. Akses FCM Registration Token (setup token FCM)

Token FCM otomatis diambil dan disimpan di dalam app.

### Di mana token diambil?

1. **Saat app dibuka**  
   Di `MainActivity`, `fetchFcmTokenIfNeeded()` memanggil  
   `FirebaseMessaging.getInstance().token`  
   dan menyimpan token lewat `FcmTokenManager.saveFcmToken(...)`.

2. **Saat token di-refresh**  
   Di `MyFirebaseMessagingService`, `onNewToken(token)` dipanggil oleh FCM ketika token berubah.  
   Lalu `sendRegistrationToServer(token)` menyimpan token (dan bisa dipakai untuk kirim ke backend).

### Cara dapat token untuk testing (Firebase Console “Test on device”)

- **Opsi A – Logcat**  
   - Jalankan app di device/emulator.  
   - Setelah token berhasil diambil, cari di Logcat filter `FCM` atau tag `FCM`:  
     `Refreshed token: ...` atau log di `sendRegistrationToServer`.  
   - Copy token tersebut.

- **Opsi B – Tampilkan di UI (opsional)**  
   Anda bisa menampilkan `PreferencesManager(context).fcmToken` (atau lewat ViewModel) di satu layar (misalnya Profile/Setting) supaya user bisa copy token untuk testing.

- **Opsi C – Kirim ke backend**  
   Di `MyFirebaseMessagingService.sendRegistrationToServer(token)` (dan saat login jika perlu), panggil API backend Anda untuk menyimpan token. Backend lalu bisa pakai token ini untuk mengirim notifikasi ke device lewat FCM API.

### Menggunakan token di Firebase Console

1. Buka **Firebase Console** → **Cloud Messaging** (atau **Engage** → **Messaging**).
2. Buat notifikasi baru (**Create your first campaign** / **New campaign** → **Firebase Notification messages**).
3. Isi **Notification title** dan **Notification text**.
4. Klik **Send test message** (atau **Test on device**).
5. Di dialog **Test on device**, pilih **Add an FCM registration token**.
6. Paste token yang sudah Anda dapat (dari log atau dari UI/backend).
7. Klik **Test** → notifikasi akan dikirim ke device yang punya token tersebut.

---

## 4. Handle pesan FCM (notifikasi saat app ditutup)

Sudah diimplementasi di:

- **`MyFirebaseMessagingService`**  
  - `onMessageReceived(remoteMessage)`  
    - Jika ada **data payload**: ambil `title`/`body` dari data (atau fallback ke `notification`), lalu panggil `showNotification(...)`.  
    - Jika hanya **notification payload**: ambil `title`/`body` dari `remoteMessage.notification` dan tetap panggil `showNotification(...)`.  
  - Dengan begitu notifikasi **tetap dibuat dan ditampilkan oleh app** lewat `NotificationManager`, sehingga **notifikasi tetap muncul walaupun app ditutup (killed/background)**.

- **AndroidManifest**  
  - Service:  
    `android:name=".fcm.MyFirebaseMessagingService"`  
    dengan `intent-filter` action `com.google.firebase.MESSAGING_EVENT`.  
  - Permission: `POST_NOTIFICATIONS` (Android 13+), `VIBRATE`, `INTERNET`.

- **Izin notifikasi**  
  Di `MainActivity`, untuk Android 13+ izin `POST_NOTIFICATIONS` diminta lewat `requestNotificationPermissionIfNeeded()`.

---

## 5. Ringkasan alur token

```
App install/buka
    → FirebaseMessaging.getInstance().token
    → Token disimpan (FcmTokenManager / PreferencesManager)
    → (Opsional) Kirim token ke backend

Token di-refresh oleh FCM
    → onNewToken(token) di MyFirebaseMessagingService
    → sendRegistrationToServer(token)
    → Simpan lokal + (opsional) kirim ke backend

Testing dari Firebase Console
    → Ambil token dari log/UI/backend
    → Test on device → paste token → Test
```

---

## 6. Agar notifikasi tetap masuk saat app di-close (killed)

Yang sudah dilakukan di app:

- **Channel notifikasi** didaftarkan di `Application.onCreate()` (MyApplication) sehingga channel sudah ada sejak app pertama dibuka; saat app di-kill dan FCM mengirim pesan, notifikasi tetap bisa tampil.
- **Notifikasi** memakai `PRIORITY_HIGH`, `DEFAULT_ALL` (sound, vibrate), dan `VISIBILITY_PUBLIC` agar tetap terlihat saat layar terkunci.
- **Backend wajib mengirim FCM** ke device (lihat §7 dan §9). Tanpa kirim FCM dari server, notifikasi tidak akan masuk saat app ditutup karena WebSocket tidak jalan.

Di beberapa HP (Xiaomi, Huawei, Oppo, dll.), jika notifikasi tetap tidak muncul saat app di-close:

- Buka **Pengaturan** → **Aplikasi** → **[Nama app]** → **Baterai** → pilih **Tanpa batasan** / **Don’t optimize**.
- Di pengaturan notifikasi app, pastikan **Izinkan notifikasi** dan **Prioritas tinggi** (jika ada).

---

## 7. Cek cepat

- **Notifikasi tidak muncul saat app ditutup**  
  - Pastikan `MyFirebaseMessagingService` terdaftar di manifest dengan `MESSAGING_EVENT`.  
  - Pastikan di `onMessageReceived` Anda memanggil `showNotification(...)` (sudah ada di kode).  
  - Pastikan izin notifikasi (Android 13+) sudah diberikan.  
  - Cek battery optimization: jangan batasi app agar FCM bisa jalan di background.

- **Token tidak muncul di log**  
  - Pastikan `google-services.json` ada di `app/` dan package name cocok.  
  - Pastikan device/emulator punya Google Play Services.  
  - Coba clear data app dan buka lagi, lalu cek Logcat lagi.

---

## 8. Kenapa notifikasi chat tidak muncul? (Backend belum kirim FCM)

**Penyebab:** Saat ada chat baru, backend Anda saat ini hanya **push via WebSocket** (`push new_message to user ...`). **Tidak ada pengiriman FCM** dari server ke device. Aplikasi Android hanya bisa menampilkan notifikasi FCM jika **backend mengirim payload FCM** ke token device tersebut.

**Yang sudah ada di app (fallback):**
- Jika app **masih jalan** (foreground/background) dan WebSocket terhubung, pesan baru dari WebSocket akan **langsung menampilkan notifikasi chat** (Balas & Tandai dibaca). Jadi notifikasi akan muncul saat ada chat selama app belum benar-benar ditutup (killed).
- Jika app **sudah ditutup (killed)**, WebSocket tidak jalan → **hanya FCM** yang bisa bikin notifikasi muncul. Untuk itu **backend wajib mengirim FCM** saat ada pesan chat baru (lihat format payload di bawah).

**Kesimpulan:** Agar notifikasi chat muncul **juga saat app ditutup**, backend harus memanggil **Firebase Admin SDK** (atau HTTP v1 FCM API) untuk mengirim data message ke FCM token penerima, dengan payload seperti di bawah.

---

## 9. Notifikasi chat (Balas & Tandai dibaca, seperti WhatsApp)

Jika backend mengirim FCM dengan **data payload** yang berisi `conversation_id`, app akan menampilkan notifikasi chat dengan dua aksi:

- **Balas** – inline reply (ketik balasan lalu kirim); pesan dikirim via API `POST .../conversations/{id}/messages`.
- **Tandai dibaca** – memanggil API `POST .../conversations/{id}/read` dan menghapus notifikasi.

Tap notifikasi akan membuka app langsung ke layar Chat untuk percakapan tersebut.

### Format payload FCM untuk chat (backend)

Kirim **data-only** (atau data + notification) dengan key berikut:

| Key | Wajib | Keterangan |
|-----|--------|------------|
| `conversation_id` | Ya | ID percakapan (untuk API balas & mark read) |
| `other_display_name` atau `sender_name` | Tidak | Nama pengirim (untuk judul notifikasi); default "Someone" |
| `title` | Tidak | Judul notifikasi (default: nama pengirim) |
| `body` atau `message` | Tidak | Isi pesan (teks notifikasi) |

Contoh payload (key-value):

- `conversation_id` = `"conv-123"`
- `other_display_name` = `"Budi"`
- `body` = `"Hai, apa kabar?"`

Dengan format ini notifikasi akan tampil dengan judul "Budi", isi "Hai, apa kabar?", dan aksi **Balas** serta **Tandai dibaca**.

---

---

## 10. Ringkasan untuk backend (agar FCM chat jalan)

1. **Simpan FCM token per user**  
   App sudah menyimpan token lokal dan bisa mengirim ke backend (endpoint simpan/update FCM token per user).

2. **Saat ada pesan chat baru**  
   Setelah menyimpan pesan dan push WebSocket, panggil juga **FCM** ke device penerima (recipient):
   - Ambil FCM token penerima dari database.
   - Kirim **data message** (bukan hanya notification) dengan key: `conversation_id`, `other_display_name` atau `sender_name`, `body` atau `message`, optional `title`.
   - Contoh (Go/Node/Python): gunakan Firebase Admin SDK, `send()` ke token, dengan `data: { "conversation_id": "...", "other_display_name": "Classer1", "body": "hlo" }`.

3. **Tanpa langkah 2**, notifikasi chat hanya akan muncul ketika app masih jalan (karena fallback dari WebSocket). Dengan langkah 2, notifikasi akan muncul **juga saat app ditutup**.

Dengan langkah di atas, setup token FCM dan notifikasi saat app ditutup sudah tercakup.
