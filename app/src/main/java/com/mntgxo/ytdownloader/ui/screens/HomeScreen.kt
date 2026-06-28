package com.mntgxo.ytdownloader.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mntgxo.ytdownloader.R
import com.mntgxo.ytdownloader.ui.theme.AccentGreen
import com.mntgxo.ytdownloader.ui.theme.AccentRed
import com.mntgxo.ytdownloader.ui.theme.SurfaceElevated
import com.mntgxo.ytdownloader.ui.theme.TextSecondary

const val UPDATE_CHANNEL_URL = "https://t.me/+ylbSCxfvWWRlYzZl"
const val SUPPORT_GROUP_URL = "https://t.me/+M1FsHW2VOL45YTA1"
const val GITHUB_URL = "https://github.com/YouTubeDownloaderApp/YouTubeDownloader"

private enum class InputMode { LINK, SEARCH }

@Composable
fun HomeScreen(
    onSubmit: (String) -> Unit,
    onSearch: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(InputMode.LINK) }
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(28.dp))

        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "MN YT Downloader",
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Spacer(Modifier.height(14.dp))

        Text(
            "MN YT Downloader",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Paste a link, choose MP4 or MP3, pick a quality.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceElevated)
                .padding(4.dp)
        ) {
            SegmentButton(
                label = "Paste Link",
                icon = Icons.Filled.Link,
                selected = mode == InputMode.LINK,
                modifier = Modifier.weight(1f),
                onClick = { mode = InputMode.LINK }
            )
            SegmentButton(
                label = "Search",
                icon = Icons.Filled.Search,
                selected = mode == InputMode.SEARCH,
                modifier = Modifier.weight(1f),
                onClick = { mode = InputMode.SEARCH }
            )
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    if (mode == InputMode.LINK) "https://youtube.com/watch?v=..."
                    else "Search YouTube..."
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = {
                if (input.isNotBlank()) {
                    if (mode == InputMode.LINK) onSubmit(input) else onSearch(input)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
        ) {
            Text(if (mode == InputMode.LINK) "Fetch Video" else "Search")
        }

        Spacer(Modifier.height(28.dp))
        HorizontalDivider(color = SurfaceElevated)
        Spacer(Modifier.height(20.dp))

        Text(
            "Open source & community",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(Modifier.height(10.dp))

        CommunityLinkRow(
            label = "⭐ View source on GitHub",
            onClick = { uriHandler.openUri(GITHUB_URL) }
        )
        CommunityLinkRow(
            label = "📢 Join the update channel",
            onClick = { uriHandler.openUri(UPDATE_CHANNEL_URL) }
        )
        CommunityLinkRow(
            label = "💬 Join the support group",
            onClick = { uriHandler.openUri(SUPPORT_GROUP_URL) }
        )

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun SegmentButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) AccentRed else Color.Transparent
    val fg = if (selected) Color.White else TextSecondary
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = fg, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CommunityLinkRow(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceElevated
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(label, color = Color.White)
        }
    }
}
