plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.moneytrees1"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.moneytrees1"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                )
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
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xjvm-default=all",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/versions/9/previous-compilation-data.bin"
            )
        }
    }
}

dependencies {
    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.database)
    kapt ("com.github.bumptech.glide:compiler:4.16.0")


    // For PDF generation
    implementation("com.itextpdf:itext7-core:7.2.3")


    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${libs.versions.lifecycle.get()}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${libs.versions.lifecycle.get()}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    //password validation utility
    implementation("at.favre.lib:bcrypt:0.9.0")

    // Coroutines
    implementation(libs.kotlinx.coroutines)

    // Security
    implementation(libs.bcrypt)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // MPAndroidChart (✅ Fixed dependency)
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.coroutines.get()}")
    androidTestImplementation("androidx.room:room-testing:${libs.versions.room.get()}")

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
}

kapt {
    correctErrorTypes = true
    useBuildCache = true
}

