plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.henrisusanto.vitmate"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.henrisusanto.vitmate"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        exclude("/META-INF/gradle/attribute")
    }

    lint {
        baseline = "lint-baseline.xml"
        abortOnError = true
        checkReleaseBuilds = false
    }
}

dependencies {
    val coreKtx = "1.15.0"
    val lifecycle = "2.9.1"
    val activity = "1.10.1"
    val composeBom = "2025.06.01"
    val material3 = "1.4.0"
    val coil = "2.7.0"
    val datastore = "1.1.7"
    val room = "2.7.4"
    val ksp = "2.1.0"
    val retrofit = "2.12.0"
    val okHttp = "5.1.0"
    val kotlinxSerialization = "1.8.1"
    val firebase = "33.14.0"
    val remoteConfig = "22.0.1"
    val ump = "23.0.1"
    val playServicesAds = "23.6.0"
    val work = "2.10.2"

    implementation("androidx.core:core-ktx:$coreKtx")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycle")
    implementation("androidx.activity:activity-compose:$activity")
    implementation("androidx.datastore:datastore:$datastore")

    implementation(platform("androidx.compose:compose-bom:$composeBom"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.runtime:runtime-rxjava3")

    implementation("io.coil-kt:coil-compose:$coil")

    implementation("androidx.room:room-runtime:$room")
    implementation("androidx.room:room-ktx:$room")
    ksp("androidx.room:room-compiler:$room")

    implementation("com.squareup.retrofit2:retrofit:$retrofit")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:$retrofit")
    implementation("com.squareup.okhttp3:okhttp:$okHttp")
    implementation("com.squareup.okhttp3:logging-interceptor:$okHttp")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerialization")

    implementation("com.google.firebase:firebase-crashlytics:$firebase")
    implementation("com.google.firebase:firebase-analytics:$firebase")
    implementation("com.google.firebase:firebase-config:$remoteConfig")
    implementation("com.google.android.ump:user-messaging-platform:$ump")
    implementation("com.google.android.play:play-services-ads:$playServicesAds")

    implementation("androidx.work:work-runtime-ktx:$work")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("io.mockk:mockk:1.14.5")
    testImplementation("org.robolectric:robolectric:4.14")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.test.ext:junit:1.2.1")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.1")
    androidTestImplementation("io.mockk:mockk-android:1.14.5")
}