package com.mntgxo.ytdownloader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mntgxo.ytdownloader.model.SearchResult
import com.mntgxo.ytdownloader.ui.theme.AccentRed
import com.mntgxo.ytdownloader.ui.theme.SurfaceElevated
import com.mntgxo.ytdownloader.ui.theme.TextSecondary

@Composable
fun SearchResultsScreen(
    query: String,
    results: List<SearchResult>,
    onBack: () -> Unit,
    onSelect: (SearchResult) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text("Results for", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text("\"$query\"", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(results.take(10)) { video ->
                SearchResultRow(video, onClick = { onSelect(video) })
            }
        }
    }
}

@Composable
private fun SearchResultRow(video: SearchResult, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = SurfaceElevated,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            val artwork = video.artworkUrl
            if (!artwork.isNullOrBlank()) {
                AsyncImage(
                    model = artwork,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(width = 96.dp, height = 64.dp).clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier.size(width = 96.dp, height = 64.dp).clip(RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = AccentRed)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    video.title ?: "Untitled",
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium
                )
                val sub = video.subtitle
                if (sub.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(sub, style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (!video.timestamp.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(video.timestamp, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}
