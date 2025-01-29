plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    //id("com.google.devtools.ksp")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.qrscanner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.qrscanner"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0-a"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.androidx.ui.android)

    // AndroidX тесты
    testImplementation(libs.junit)
    testImplementation(libs.core.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.mlkit.barcode.scanning)

    // Работа с камерой
    implementation (libs.androidx.camera.core)
    implementation (libs.androidx.camera.camera2)
    implementation (libs.androidx.camera.lifecycle)
    implementation (libs.androidx.camera.view)

    implementation (libs.androidx.room.runtime)
    androidTestImplementation(libs.core.testing)

    //noinspection KaptUsageInsteadOfKsp
    kapt (libs.androidx.room.compiler)
    implementation (libs.androidx.room.ktx)
    // Room тестирование
    testImplementation (libs.room.testing)

    // Lifecycle
    implementation (libs.androidx.lifecycle.common.java8)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.livedata.ktx)
    implementation (libs.androidx.lifecycle.runtime.ktx)


    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    implementation(libs.androidx.constraintlayout)
    // To use constraintlayout in compose
    implementation(libs.androidx.constraintlayout.compose)
}