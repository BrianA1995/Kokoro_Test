plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.kokoro_test"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.kokoro_test"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Important: Configure NDK for native library support
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        // Prevent duplicate .so files if needed
        jniLibs {
            pickFirsts.add("lib/**/libsherpa-onnx-jni.so")
            pickFirsts.add("lib/**/libonnxruntime.so")
        }
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // ============================================
    // SHERPA-ONNX TTS LIBRARY
    // ============================================
    // Method 1: Use published AAR (RECOMMENDED)
    // Uncomment ONE of the following:

    // Option A: From Maven/Gradle repository (if available)
    // implementation("com.k2fsa:sherpa-onnx:1.12.18")

    // Option B: Use local AAR file
    // Place sherpa-onnx-1.12.18.aar in app/libs/
    implementation(files("libs/sherpa-onnx-1.12.18.aar"))

    // ============================================
    // If using local AAR, you may need ONNX Runtime dependency
    // Check AAR contents to see if it's bundled or needs separate import
    // implementation("com.microsoft.onnxruntime:onnxruntime-android:1.16.0")
    // ============================================

    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// ============================================
// IMPORTANT NOTES:
// ============================================
// 1. If using local AAR file:
//    - Place sherpa-onnx-1.12.18.aar in app/libs/ directory
//    - Gradle will automatically find it with the files() dependency
//
// 2. If AAR is missing native libraries:
//    - Extract .so files from AAR (it's a ZIP file)
//    - Place them in app/src/main/jniLibs/<ABI>/ directories:
//      - app/src/main/jniLibs/arm64-v8a/libsherpa-onnx-jni.so
//      - app/src/main/jniLibs/armeabi-v7a/libsherpa-onnx-jni.so
//      - etc.
//
// 3. Model files MUST be in:
//    app/src/main/assets/kokoro-en-v0_19/
//    (or whatever directory name you configure in MainActivity.kt)
//
// 4. After adding AAR or changing dependencies:
//    - File -> Sync Project with Gradle Files
//    - Build -> Clean Project
//    - Build -> Rebuild Project
// ============================================
