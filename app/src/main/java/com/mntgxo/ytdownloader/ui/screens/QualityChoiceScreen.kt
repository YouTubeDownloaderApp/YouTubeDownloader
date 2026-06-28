package com.mntgxo.ytdownloader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mntgxo.ytdownloader.model.DownloadKind
import com.mntgxo.ytdownloader.ui.theme.AccentGreen
import com.mntgxo.ytdownloader.ui.theme.AccentRed
import com.mntgxo.ytdownloader.ui.theme.SurfaceElevated
import com.mntgxo.ytdownloader.ui.theme.TextSecondary

@Composable
fun QualityChoiceScreen(
    title: String,
    kind: DownloadKind,
    qualities: List<Int>,
    onBack: () -> Unit,
    onChooseQuality: (Int) -> Unit
) {
    val accent = if (kind == DownloadKind.MP4) AccentRed else AccentGreen

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Choose quality", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

        Spacer(Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(qualities.sortedDescending()) { q ->
                val label = if (kind == DownloadKind.MP4) "${q}p" else "${q}kbps"
                Surface(
                    onClick = { onChooseQuality(q) },
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceElevated,
                    modifier = Modifier.height(64.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(label, fontWeight = FontWeight.SemiBold, color = accent)
                    }
                }
            }
        }
    }
}
