# MN YT Downloader — Android

A native Android (Kotlin + Jetpack Compose) port of the [MN YouTube Downloader Telegram bot](https://github.com/YouTubeDownloaderApp/YouTubeDownloader). Paste a YouTube link (or search), pick MP4 or MP3, choose a quality, and the file downloads straight to your device's **Downloads/MN_YT_Downloads** folder — no Telegram required.

This project is **open source**: https://github.com/YouTubeDownloaderApp/YouTubeDownloader

## Features

- Paste any YouTube URL (watch, shorts, youtu.be, embed) or share one into the app
- `/search`-style in-app YouTube search
- MP4 (video) and MP3 (audio) formats with quality picker (144p–1080p / 92–320kbps, depending on what the API reports)
- Foreground service with a progress notification so downloads survive backgrounding
- Saves via `MediaStore` on Android 10+ and to the legacy public Downloads folder on older versions

## Architecture

The app is a thin client for the same backend the original bot used (`Config.YT_API` → `https://youtube-downloader.mn-bots.workers.dev`), hitting the same four endpoints:

| Endpoint | Purpose |
|---|---|
| `GET /info?url=` | Title + thumbnail for a video |
| `GET /mp4?url=&quality=` | Available qualities, then a direct MP4 URL |
| `GET /mp3?url=&quality=` | Available qualities, then a direct MP3 URL |
| `GET /search?s=` | YouTube search results |

| Bot concept | Android equivalent |
|---|---|
| `extract_url()` / `vid_id_from_url()` regex | `YouTubeUrlUtil` |
| `api_get()` w/ per-endpoint timeouts & retries | `network/ApiClient.kt` (Retrofit + OkHttp) |
| `download_file()` + Telegram upload | `download/FileDownloader.kt` → device storage |
| Inline keyboards (`quality_buttons`, `search_result_buttons`) | Compose screens (`QualityChoiceScreen`, `SearchResultsScreen`) |
| Progress message edits | `DownloadForegroundService` notification + broadcast → Compose state |

## Building

### Locally
```
./gradlew assembleDebug
```
APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

### CI (GitHub Actions)
Push to the repo, or trigger manually — `.github/workflows/build-apk.yml` builds a debug APK on every push and uploads it as a workflow artifact.

## Community

- 📢 Update channel: https://t.me/+ylbSCxfvWWRlYzZl
- 💬 Support group: https://t.me/+M1FsHW2VOL45YTA1

## Disclaimer

Downloading content from YouTube may conflict with YouTube's Terms of Service depending on how the app is used and distributed. This project is provided as-is for personal/educational use; you're responsible for how you use and distribute it.

## License

GPL-3.0, matching the original bot repository.
