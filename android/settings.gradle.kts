pluginManagement {
    val flutterSdkPath = run {
        val properties = java.util.Properties()
        file("local.properties").inputStream().use { properties.load(it) }
        val flutterSdkPath = properties.getProperty("flutter.sdk")
        require(flutterSdkPath != null) { "flutter.sdk not set in local.properties" }
        flutterSdkPath
    }

    includeBuild("$flutterSdkPath/packages/flutter_tools/gradle")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("dev.flutter.flutter-plugin-loader") version "1.0.0"

    // ✅ AGPバージョンを 8.6.0 に下げる（Android Studio 対応）
    id("com.android.application") version "8.6.0" apply false

    // ✅ Kotlin バージョンも Flutter/Compose に適合させる
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}

include(":app")
