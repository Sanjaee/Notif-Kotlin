plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 🔐 SIGNING CONFIG (Opsional - hanya untuk release build jika keystore ada)
    val keystoreFile = file("release.keystore")
    val hasKeystore = keystoreFile.exists()
    
    if (!hasKeystore) {
        println("ℹ️  Info: release.keystore tidak ditemukan (normal untuk development)")
        println("   Build release akan menggunakan debug keystore")
        println("   Untuk production build, buat keystore dengan:")
        println("   keytool -genkey -v -keystore app/release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000")
    }
    
    signingConfigs {
        if (hasKeystore) {
            create("release") {
                storeFile = keystoreFile
                storePassword = "123123"
                keyAlias = "release"
                keyPassword = "123123"
                
                println("✅ Signing Config Release ditemukan:")
                println("   - Keystore: ${keystoreFile.absolutePath}")
                println("   - Key Alias: release")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // 🔑 Gunakan release signing config hanya jika keystore ada
            if (hasKeystore && signingConfigs.findByName("release") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // default debug config (menggunakan debug keystore)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // ViewModel
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // DataStore
    implementation(libs.datastore.preferences)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // 🔥 Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)

    // 🔐 Google Sign In
    implementation(libs.play.services.auth)

    // Image Loading
    implementation(libs.coil.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
