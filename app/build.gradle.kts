plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.triptales.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.triptales.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["mapsApiKey"] = project.findProperty("MAPS_API_KEY") ?: ""
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

    // retrofit e gson
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.material3)
    implementation(libs.material)

    // position
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.maps.compose)

    // map utils
    implementation(libs.android.maps.utils)

    //  icons
    implementation(libs.androidx.material.icons.extended)

    // coil
    implementation(libs.coil.kt.coil.compose)
    implementation(libs.coil.network.okhttp)

    //pull to refresh
    implementation(libs.androidx.material3.window.size.class1)

    // image cropper
    implementation("com.vanniktech:android-image-cropper:4.6.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    implementation ("androidx.appcompat:appcompat:1.7.0-alpha03")
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.activity:activity-compose:1.8.0")
}