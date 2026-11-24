plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ares.scan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ares.scan"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.2"
    }
}


dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
}
