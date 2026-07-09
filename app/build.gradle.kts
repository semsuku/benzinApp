import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
val backendBaseUrl = localProperties.getProperty("BACKEND_BASE_URL") ?: "https://api.semsuku.uk/"
val backendApiKey = localProperties.getProperty("BACKEND_API_KEY") ?: "benzinapp_secret_key_123"
val cleanBaseUrl = backendBaseUrl.trim().replace("\"", "").replace("'", "")
val cleanBackendKey = backendApiKey.trim().replace("\"", "").replace("'", "")

android {
    namespace = "com.example.benzinapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.franc.benzinapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 5
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        buildConfigField("String", "BACKEND_BASE_URL", "\"$cleanBaseUrl\"")
        buildConfigField("String", "BACKEND_API_KEY", "\"$cleanBackendKey\"")
    }

    signingConfigs {
        create("release") {
            val storeFileVal = localProperties.getProperty("RELEASE_STORE_FILE")
            if (storeFileVal != null) {
                storeFile = rootProject.file(storeFileVal)
                storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material.icons.extended)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Generative AI (Gemini)
    implementation(libs.google.generativeai)

    // Retrofit & OkHttp
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)

    // Vico Compose
    implementation(libs.vico.compose)
    implementation(libs.vico.core)

    // ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // AdMob
    implementation(libs.play.services.ads)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
