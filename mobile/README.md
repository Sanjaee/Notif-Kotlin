# Kotlin Mobile Android App

Aplikasi Android dengan Kotlin yang terintegrasi dengan API authentication backend. Aplikasi ini menggunakan Jetpack Compose untuk UI dan mengimplementasikan fitur login, register, verify OTP, forgot password, dan verify email.

## Fitur

- ✅ Login
- ✅ Register
- ✅ Verify OTP
- ✅ Resend OTP
- ✅ Forgot Password
- ✅ Verify Reset Password
- ✅ Verify Email
- ✅ Token Storage (DataStore)
- ✅ Navigation dengan Jetpack Compose Navigation

## Cara Install & Setup

### 1. Prerequisites

Pastikan Anda sudah menginstall:

- **Java JDK 17 atau lebih baru** (disarankan JDK 17)
- **Android Studio** (versi terbaru, disarankan Hedgehog atau lebih baru)
- **Android SDK** (akan terinstall otomatis dengan Android Studio)

### 2. Install Android Studio

1. Download Android Studio dari [developer.android.com](https://developer.android.com/studio)
2. Install Android Studio sesuai dengan OS Anda
3. Saat pertama kali dibuka, Android Studio akan meminta setup SDK:
   - Pilih "Standard" installation
   - Tunggu proses download SDK selesai

### 3. Setup Android SDK

1. Buka Android Studio
2. Pilih **Tools → SDK Manager**
3. Di tab **SDK Platforms**, pastikan Anda menginstall:
   - Android 14.0 (API Level 34) atau lebih tinggi
   - Android 13.0 (API Level 33) - untuk kompatibilitas
4. Di tab **SDK Tools**, pastikan menginstall:
   - Android SDK Build-Tools
   - Android SDK Platform-Tools
   - Android SDK Command-line Tools
   - Android Emulator (jika ingin menggunakan emulator)

### 4. Setup Environment Variables (Optional tapi Recommended)

#### Windows:
1. Buka **System Properties → Environment Variables**
2. Tambahkan variabel baru:
   - `ANDROID_HOME`: `C:\Users\<YourUsername>\AppData\Local\Android\Sdk`
   - Tambahkan ke PATH: `%ANDROID_HOME%\platform-tools` dan `%ANDROID_HOME%\tools`

#### macOS/Linux:
Tambahkan ke `~/.bashrc` atau `~/.zshrc`:
```bash
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/tools
```

### 5. Clone/Buka Project dari GitHub

#### Clone dengan Git:
```bash
# Clone repository
git clone <repository-url>
cd <repository-name>/kotlin
```

#### Atau buka langsung di Android Studio:
1. Buka Android Studio
2. Pilih **File → New → Project from Version Control → Git**
3. Masukkan URL repository
4. Pilih folder `kotlin` dari project yang sudah di-clone
5. Tunggu Gradle sync selesai (akan download dependencies otomatis)

> **Note:** Project ini sudah dikonfigurasi untuk langsung bisa dijalankan setelah di-clone. Debug build bisa langsung digunakan tanpa setup tambahan!

### 6. Setup Keystore (Opsional - Hanya untuk Release Build)

Untuk build **debug**, Anda tidak perlu setup keystore. Project sudah bisa langsung dijalankan!

Untuk build **release** (production), Anda perlu membuat keystore:

#### Cara 1: Menggunakan Android Studio
1. Buka **Build → Generate Signed Bundle / APK**
2. Pilih **Create new...** untuk membuat keystore baru
3. Isi informasi keystore dan simpan file ke `app/release.keystore`
4. Copy file `app/keystore.properties.example` menjadi `app/keystore.properties`
5. Edit `app/keystore.properties` dan isi dengan informasi keystore Anda:
   ```properties
   storePassword=your_store_password
   keyPassword=your_key_password
   keyAlias=release
   storeFile=release.keystore
   ```

#### Cara 2: Menggunakan Command Line (keytool)
```bash
# Windows (Command Prompt atau PowerShell)
cd app
keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000

# macOS/Linux
cd app
keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000
```

Ikuti instruksi yang muncul untuk mengisi informasi keystore. Setelah selesai:
1. Copy file `app/keystore.properties.example` menjadi `app/keystore.properties`
2. Edit `app/keystore.properties` dan isi dengan password yang Anda gunakan

> **Penting:** 
> - File `release.keystore` dan `keystore.properties` **TIDAK** akan di-commit ke Git (sudah di-ignore)
> - Simpan keystore dengan aman! Jika hilang, Anda tidak bisa update aplikasi di Play Store
> - Untuk development/testing, Anda bisa skip step ini dan langsung build debug

### 7. Konfigurasi API Base URL

API base URL sudah dikonfigurasi di:
```
app/src/main/java/com/example/myapplication/data/api/ApiClient.kt
```

Default URL: `https://express-template-login.vercel.app/`

Jika ingin mengubah, edit konstanta `BASE_URL` di file tersebut.

### 8. Build Project

#### Menggunakan Android Studio:

1. **Sync Project dengan Gradle Files:**
   - Klik **File → Sync Project with Gradle Files**
   - Tunggu proses sync selesai

2. **Build APK Debug:**
   - Pilih **Build → Build Bundle(s) / APK(s) → Build APK(s)**
   - Tunggu proses build selesai
   - APK akan berada di: `app/build/outputs/apk/debug/app-debug.apk`

3. **Build APK Release (untuk production):**
   - Pilih **Build → Generate Signed Bundle / APK**
   - Pilih **APK**
   - Buat keystore baru atau gunakan yang sudah ada
   - Isi informasi keystore
   - Pilih build variant: **release**
   - APK akan berada di: `app/build/outputs/apk/release/app-release.apk`

#### Menggunakan Command Line (Terminal/CMD):

1. **Buka terminal di root folder project** (`kotlin/`)

2. **Windows:**
   ```cmd
   gradlew.bat assembleDebug
   ```
   APK akan ada di: `app\build\outputs\apk\debug\app-debug.apk`

3. **macOS/Linux:**
   ```bash
   ./gradlew assembleDebug
   ```
   APK akan ada di: `app/build/outputs/apk/debug/app-debug.apk`

4. **Build Release:**
   ```bash
   # Windows
   gradlew.bat assembleRelease
   
   # macOS/Linux
   ./gradlew assembleRelease
   ```
   
   > **Note:** Jika keystore belum dibuat, build release masih bisa berhasil tetapi akan menggunakan debug keystore (tidak untuk production). Untuk production build, pastikan Anda sudah setup keystore terlebih dahulu (lihat langkah 6).

### 9. Install APK ke Device

#### Menggunakan Android Studio:
1. Hubungkan device Android via USB
2. Aktifkan **USB Debugging** di device (Settings → Developer Options → USB Debugging)
3. Klik tombol **Run** (hijau) di Android Studio
4. Pilih device/emulator
5. Aplikasi akan terinstall dan terbuka otomatis

#### Menggunakan ADB (Command Line):
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### Manual Install:
1. Transfer file APK ke device Android
2. Buka file APK di device
3. Izinkan install dari sumber tidak dikenal jika diminta
4. Install aplikasi

### 10. Menjalankan di Emulator

1. Buka **Tools → Device Manager** di Android Studio
2. Klik **Create Device**
3. Pilih device yang diinginkan (contoh: Pixel 5)
4. Download system image jika diperlukan
5. Klik **Finish**
6. Pilih emulator yang sudah dibuat dan klik **Run**

## Struktur Project

```
kotlin/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/myapplication/
│   │   │   │   ├── data/
│   │   │   │   │   ├── api/           # API service & client
│   │   │   │   │   ├── model/         # Data models
│   │   │   │   │   ├── preferences/   # DataStore untuk token storage
│   │   │   │   │   └── repository/    # Repository pattern
│   │   │   │   ├── navigation/        # Navigation setup
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screen/        # UI Screens (Compose)
│   │   │   │   │   ├── theme/         # App theme
│   │   │   │   │   └── viewmodel/     # ViewModels
│   │   │   │   └── MainActivity.kt
│   │   │   └── AndroidManifest.xml
│   │   └── test/                      # Unit tests
│   └── build.gradle.kts               # App-level dependencies
├── build.gradle.kts                   # Project-level config
├── gradle/
│   └── libs.versions.toml             # Version catalog
└── settings.gradle.kts
```

## Dependencies Utama

- **Jetpack Compose**: UI framework
- **Navigation Compose**: Navigation
- **Retrofit**: HTTP client untuk API calls
- **Gson**: JSON parsing
- **DataStore**: Token storage (menggantikan SharedPreferences)
- **ViewModel & LiveData**: Architecture components
- **Coroutines**: Async programming

## API Endpoints

Aplikasi menggunakan API base URL: `https://express-template-login.vercel.app/api/v1/auth`

Endpoints yang digunakan:
- `POST /register` - Register user baru
- `POST /login` - Login
- `POST /verify-otp` - Verify OTP code
- `POST /resend-otp` - Resend OTP
- `POST /forgot-password` - Request reset password
- `POST /verify-reset-password` - Verify reset password dengan OTP
- `POST /verify-email` - Verify email dengan token
- `GET /me` - Get current user (protected, requires token)

## Troubleshooting

### Error: "SDK location not found"
- Pastikan `local.properties` ada di root project dengan isi:
  ```
  sdk.dir=C:\\Users\\<YourUsername>\\AppData\\Local\\Android\\Sdk
  ```
  (Windows) atau path SDK Anda di macOS/Linux
- Android Studio biasanya akan membuat file ini secara otomatis saat pertama kali membuka project

### Error: "Gradle sync failed"
- Pastikan koneksi internet stabil (untuk download dependencies)
- Coba **File → Invalidate Caches → Invalidate and Restart**
- Hapus folder `.gradle` dan `build` di root project, lalu sync lagi:
  ```bash
  # Windows
  rmdir /s /q .gradle build
  gradlew.bat clean
  
  # macOS/Linux
  rm -rf .gradle build
  ./gradlew clean
  ```

### Error: "release.keystore tidak ditemukan" saat build release
- **Ini normal untuk pertama kali!** Project sudah dikonfigurasi untuk bekerja tanpa keystore
- Untuk build **debug**, tidak perlu keystore - langsung jalankan `./gradlew assembleDebug` atau build dari Android Studio
- Untuk build **release** yang signed, buat keystore terlebih dahulu (lihat langkah 6 di atas)
- Jika Anda hanya ingin test build release tanpa signing, build masih bisa berhasil tetapi akan menggunakan debug keystore

### APK tidak bisa diinstall
- Pastikan device mengizinkan install dari sumber tidak dikenal (Settings → Security → Unknown Sources)
- Pastikan APK untuk architecture yang benar (armeabi-v7a, arm64-v8a, x86, x86_64)
- Coba uninstall aplikasi sebelumnya jika sudah ada dengan `adb uninstall com.example.myapplication`
- Pastikan device memiliki storage yang cukup

### Error saat connect ke API
- Pastikan device/emulator memiliki koneksi internet
- Check API base URL di `ApiClient.kt`
- Pastikan API server sedang berjalan dan accessible
- Untuk emulator, pastikan tidak ada firewall yang memblokir koneksi

### Project tidak bisa di-clone atau dijalankan
- Pastikan semua prerequisites sudah terinstall (JDK 17+, Android Studio)
- Pastikan `local.properties` dibuat (Android Studio biasanya membuat ini otomatis)
- Coba **File → Sync Project with Gradle Files** di Android Studio
- Jika masih error, coba clean dan rebuild project

## Quick Start (Setelah Clone)

Jika Anda baru saja clone project ini dan ingin langsung menjalankannya:

1. **Buka project di Android Studio**
   - File → Open → pilih folder `kotlin`
   - Tunggu Gradle sync selesai

2. **Build dan Run Debug** (tidak perlu setup apapun)
   - Klik tombol **Run** (▶️) atau tekan `Shift + F10`
   - Pilih device/emulator
   - Aplikasi akan langsung terinstall dan berjalan

3. **Atau build APK secara manual:**
   ```bash
   # Windows
   gradlew.bat assembleDebug
   
   # macOS/Linux
   ./gradlew assembleDebug
   ```
   APK akan ada di: `app/build/outputs/apk/debug/app-debug.apk`

> **Catatan:** Untuk build release yang signed (production), Anda perlu setup keystore terlebih dahulu (lihat langkah 6 di atas).

## Development

### Menambahkan Screen Baru

1. Buat ViewModel di `ui/viewmodel/`
2. Buat Screen di `ui/screen/`
3. Tambahkan route di `navigation/NavGraph.kt`
4. Update `Screen` sealed class

### Mengubah API Base URL

Edit file `app/src/main/java/com/example/myapplication/data/api/ApiClient.kt`:
```kotlin
private const val BASE_URL = "https://your-api-url.com/"
```

## License

This project is part of the template extension collection.

