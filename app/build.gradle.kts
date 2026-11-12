plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    // Your android configuration here
}

dependencies {
    implementation("com.github.yausername.youtubedl-android:library:0.13.+")
    implementation("com.github.yausername.youtubedl-android:ffmpeg:0.13.+")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("io.coil-kt:coil-compose:2.6.0")
    
    // Add for queue management
    implementation("androidx.lifecycle:lifecycle-service:2.8.7")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
}
