plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.belectronicsvp.bluetooth"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    implementation(project(":core-protocol"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("androidx.core:core-ktx:1.13.1")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}
