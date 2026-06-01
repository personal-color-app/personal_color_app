import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

fun projectSecret(name: String): String {
    val props = Properties()
    val candidates = listOf(
        rootProject.file("local.properties"),
        rootProject.file("../local.properties"),
    )
    candidates.filter { it.exists() }.forEach { file ->
        file.inputStream().use(props::load)
    }
    return props.getProperty(name).orEmpty()
}

android {
    namespace = "com.oliveme.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.oliveme.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["kakaoNativeAppKey"] = projectSecret("KAKAO_NATIVE_APP_KEY")

        buildConfigField("String", "GEMINI_API_KEY", "\"${projectSecret("GEMINI_API_KEY")}\"")
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"${projectSecret("KAKAO_NATIVE_APP_KEY")}\"")
        buildConfigField("String", "KAKAO_REST_API_KEY", "\"${projectSecret("KAKAO_REST_API_KEY")}\"")
        buildConfigField("String", "GEMINI_MODEL", "\"gemini-3.5-flash\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/LICENSE.md",
            "META-INF/LICENSE-notice.md",
        )
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.kakao.sdk:v2-user:2.20.6")
    implementation("com.kakao.maps.open:android:2.13.2")
    implementation("org.tensorflow:tensorflow-lite:2.16.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
