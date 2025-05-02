// Top-level build file where you can add configuration options common to all sub-projects/modules
plugins {
    // Android Gradle Plugin - applied to app module only (apply false)
    alias(libs.plugins.android.application) apply false

    // Kotlin Android Plugin - applied to app module only (apply false)
    alias(libs.plugins.kotlin.android) apply false

    // Kotlin Annotation Processing Tool - needed for Room
    // Note: This is declared here but applied in the app module
    alias(libs.plugins.kotlin.kapt) apply false
}

// Optional: You can add common buildscript configurations here
buildscript {
    // If you need to add any repositories or dependencies for plugins
    // that aren't managed by version catalogs, you can add them here
    repositories {
        google()
        mavenCentral()
    }

}

// Optional: Clean task configuration
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}