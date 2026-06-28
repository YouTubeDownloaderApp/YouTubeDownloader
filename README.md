# Youtube Downloader 

A native Android (Kotlin + Jetpack Compose) [YouTube Downloader](https://github.com/YouTubeDownloaderApp/YouTubeDownloader). Paste a YouTube link (or search), pick MP4 or MP3, choose a quality, and the file downloads straight to your device's **Downloads/MN_YT_Downloads** folder — no Telegram required.

This project is **open source**: https://github.com/YouTubeDownloaderApp/YouTubeDownloader

## Features

- Paste any YouTube URL (watch, shorts, youtu.be, embed) or share one into the app
- `/search`-style in-app YouTube search
- MP4 (video) and MP3 (audio) formats with quality picker (144p–1080p / 92–320kbps, depending on what the API reports)
- Foreground service with a progress notification so downloads survive backgrounding
- Saves via `MediaStore` on Android 10+ and to the legacy public Downloads folder on older versions

## Building

### Locally
```
./gradlew assembleDebug
```
APK lands in `app/build/outputs/apk/debug/app-debug.apk`.


## Community

- 📢 Update channel: https://t.me/+ylbSCxfvWWRlYzZl
- 💬 Support group: https://t.me/+M1FsHW2VOL45YTA1

## Disclaimer

Downloading content from YouTube may conflict with YouTube's Terms of Service depending on how the app is used and distributed. This project is provided as-is for personal/educational use; you're responsible for how you use and distribute it.


## License

This project is licensed under the GNU Public License. See the [LICENSE](LICENSE) file for details.
