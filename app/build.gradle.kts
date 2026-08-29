import com.google.firebase.appdistribution.gradle.firebaseAppDistribution

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    id("com.google.gms.google-services")
    alias(libs.plugins.firebase.appdistribution)
}

android {
    namespace = "com.eleyas.expensetracker"

    compileSdk = 37

    defaultConfig {
        applicationId = "com.eleyas.expensetracker"

        minSdk = 24
        targetSdk = 37

        versionCode = 4
        versionName = "1.2.1"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            firebaseAppDistribution {
                artifactType = "APK"
                releaseNotes = "New updates and UI improvements"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    // AndroidX / Jetpack Compose
    implementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    implementation(
        libs.androidx.activity.compose
    )

    implementation(libs.androidx.compose.runtime)

    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    implementation(
        libs.androidx.compose.material3
    )

    implementation("androidx.compose.material:material-icons-extended")

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.graphics
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    implementation(
        libs.androidx.core.ktx
    )

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Firebase
    implementation(
        platform(
            "com.google.firebase:firebase-bom:34.16.0"
        )
    )

    implementation(
        "com.google.firebase:firebase-auth"
    )

    implementation(
        "com.google.firebase:firebase-firestore"
    )

    implementation(
        "com.google.mlkit:text-recognition:16.0.1"
    )

    // Google Sign-In / Credential Manager
    implementation(
        "androidx.credentials:credentials:1.3.0"
    )

    implementation(
        "androidx.credentials:credentials-play-services-auth:1.3.0"
    )

    implementation(
        "com.google.android.libraries.identity.googleid:googleid:1.1.1"
    )

    // Testing
    testImplementation(
        libs.junit
    )

    androidTestImplementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}
