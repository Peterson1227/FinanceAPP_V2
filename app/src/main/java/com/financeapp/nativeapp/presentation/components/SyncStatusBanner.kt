package com.financeapp.nativeapp.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financeapp.nativeapp.presentation.theme.*

@Composable
fun SyncStatusBanner(
    pendingCount: Int,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    // Mostra se offline OU se há itens pendentes
    val showBanner = !isOnline || pendingCount > 0

    AnimatedVisibility(
        visible = showBanner,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        val backgroundColor: androidx.compose.ui.graphics.Color
        val icon: androidx.compose.ui.graphics.vector.ImageVector
        val message: String
        val iconTint: androidx.compose.ui.graphics.Color

        when {
            !isOnline && pendingCount > 0 -> {
                backgroundColor = OfflineRed.copy(alpha = 0.12f)
                icon = Icons.Default.WifiOff
                iconTint = OfflineRed
                message = "🔴 Offline · $pendingCount item(ns) na fila de sincronização"
            }
            !isOnline -> {
                backgroundColor = SyncPending.copy(alpha = 0.10f)
                icon = Icons.Default.WifiOff
                iconTint = SyncPending
                message = "📡 Sem conexão · Alterações serão salvas localmente"
            }
            pendingCount > 0 -> {
                backgroundColor = SyncSyncing.copy(alpha = 0.10f)
                icon = Icons.Default.Sync
                iconTint = SyncSyncing
                message = "🔄 Sincronizando $pendingCount item(ns)..."
            }
            else -> {
                backgroundColor = SyncSynced.copy(alpha = 0.10f)
                icon = Icons.Default.CloudDone
                iconTint = SyncSynced
                message = "✅ Tudo sincronizado"
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
