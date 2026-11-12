dependencies {
    // Existing dependencies...
    
    // Add for yt-dlp integration
    implementation("com.github.yausername.youtubedl-android:library:0.13.+")
    implementation("com.github.yausername.youtubedl-android:ffmpeg:0.13.+")
    
    // For cookie handling
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // For improved UI
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("io.coil-kt:coil-compose:2.6.0")
}
