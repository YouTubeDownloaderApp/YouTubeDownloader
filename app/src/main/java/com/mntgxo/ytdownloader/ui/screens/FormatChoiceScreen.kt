package com.mntgxo.ytdownloader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mntgxo.ytdownloader.model.DownloadKind
import com.mntgxo.ytdownloader.ui.theme.AccentGreen
import com.mntgxo.ytdownloader.ui.theme.AccentRed
import com.mntgxo.ytdownloader.ui.theme.SurfaceElevated
import com.mntgxo.ytdownloader.ui.theme.TextSecondary

@Composable
fun FormatChoiceScreen(
    title: String,
    thumbnail: String?,
    onBack: () -> Unit,
    onChooseFormat: (DownloadKind) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        TopRow(onBack)

        Spacer(Modifier.height(16.dp))

        if (!thumbnail.isNullOrBlank()) {
            AsyncImage(
                model = thumbnail,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.height(16.dp))
        }

        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))
        Text(
            "Choose a format to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            FormatCard(
                label = "MP4",
                sublabel = "Video",
                icon = Icons.Filled.MovieFilter,
                accent = AccentRed,
                modifier = Modifier.weight(1f),
                onClick = { onChooseFormat(DownloadKind.MP4) }
            )
            FormatCard(
                label = "MP3",
                sublabel = "Audio",
                icon = Icons.Filled.MusicNote,
                accent = AccentGreen,
                modifier = Modifier.weight(1f),
                onClick = { onChooseFormat(DownloadKind.MP3) }
            )
        }
    }
}

@Composable
private fun TopRow(onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
        }
        Text("Choose format", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun FormatCard(
    label: String,
    sublabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceElevated
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(10.dp))
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(sublabel, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}
