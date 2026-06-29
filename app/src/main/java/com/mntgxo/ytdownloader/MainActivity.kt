package com.mntgxo.ytdownloader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mntgxo.ytdownloader.download.DownloadForegroundService
import com.mntgxo.ytdownloader.ui.MainViewModel
import com.mntgxo.ytdownloader.ui.ScreenState
import com.mntgxo.ytdownloader.ui.screens.*
import com.mntgxo.ytdownloader.ui.theme.MNYTDownloaderTheme
import com.mntgxo.ytdownloader.util.FormatUtil

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var pendingTitle: String = ""

    private val progressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                DownloadForegroundService.ACTION_PROGRESS -> {
                    val pct = intent.getIntExtra(DownloadForegroundService.EXTRA_PROGRESS_PCT, 0)
                    val speed = intent.getDoubleExtra(DownloadForegroundService.EXTRA_SPEED, 0.0)
                    viewModel.onProgressUpdate(pendingTitle, pct, FormatUtil.fmtSpeed(speed))
                }
                DownloadForegroundService.ACTION_COMPLETE -> {
                    val path = intent.getStringExtra(DownloadForegroundService.EXTRA_RESULT_PATH).orEmpty()
                    val mime = intent.getStringExtra(DownloadForegroundService.EXTRA_RESULT_MIME).orEmpty()
                    viewModel.onDownloadComplete(pendingTitle, path, mime)
                }
                DownloadForegroundService.ACTION_FAILED -> {
                    val err = intent.getStringExtra(DownloadForegroundService.EXTRA_ERROR).orEmpty()
                    viewModel.onDownloadFailed(err)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        handleIncomingShare(intent)

        setContent {
            MNYTDownloaderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val state by viewModel.state.collectAsState()

                    DisposableEffect(Unit) {
                        val filter = IntentFilter().apply {
                            addAction(DownloadForegroundService.ACTION_PROGRESS)
                            addAction(DownloadForegroundService.ACTION_COMPLETE)
                            addAction(DownloadForegroundService.ACTION_FAILED)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            registerReceiver(progressReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
                        } else {
                            @Suppress("UnspecifiedRegisterReceiverFlag")
                            registerReceiver(progressReceiver, filter)
                        }
                        onDispose { unregisterReceiver(progressReceiver) }
                    }

                    when (val s = state) {
                        is ScreenState.Idle -> HomeScreen(
                            onSubmit = { viewModel.submitUrlOrText(it) },
                            onSearch = { viewModel.search(it) }
                        )
                        is ScreenState.Loading -> LoadingScreen()
                        is ScreenState.SearchResults -> SearchResultsScreen(
                            query = s.query, results = s.results,
                            onBack = { viewModel.reset() },
                            onSelect = { viewModel.selectSearchResult(it) }
                        )
                        is ScreenState.FormatChoice -> FormatChoiceScreen(
                            title = s.title, thumbnail = s.thumbnail,
                            onBack = { viewModel.reset() },
                            onChooseFormat = { kind ->
                                pendingTitle = s.title
                                viewModel.chooseFormat(s.videoId, s.title, s.sourceUrl, kind)
                            }
                        )
                        is ScreenState.QualityChoice -> QualityChoiceScreen(
                            title = s.title, kind = s.kind, qualities = s.qualities,
                            onBack = { viewModel.reset() },
                            onChooseQuality = { q ->
                                pendingTitle = s.title
                                viewModel.chooseQuality(s.videoId, s.title, s.sourceUrl, s.kind, q)
                            }
                        )
                        is ScreenState.Downloading -> DownloadingScreen(
                            title = s.title, progressPct = s.progressPct, speedLabel = s.speedLabel
                        )
                        is ScreenState.Complete -> DownloadCompleteScreen(
                            title = s.title,
                            onPlay = { playFile(s.filePath, s.mimeType) },
                            onDone = { viewModel.reset() }
                        )
                        is ScreenState.Error -> ErrorScreen(
                            message = s.message, onRetry = { viewModel.reset() }
                        )
                    }
                }
            }
        }
    }

    private fun playFile(filePath: String, mimeType: String) {
        try {
            val uri = Uri.parse(filePath)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Play with..."))
        } catch (e: Exception) { /* no player installed */ }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingShare(intent)
    }

    private fun handleIncomingShare(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.takeIf { it.isNotBlank() }
                ?.let { viewModel.submitUrlOrText(it) }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001
                )
            }
        }
    }
}
