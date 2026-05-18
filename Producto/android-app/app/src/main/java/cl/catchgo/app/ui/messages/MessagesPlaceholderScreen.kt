package cl.catchgo.app.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.catchgo.app.domain.model.AppNotification
import cl.catchgo.app.ui.components.EmptyState
import cl.catchgo.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    Column(modifier = modifier.fillMaxSize().background(Gray50)) {
        // Beautiful Blue Gradient Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(colors = listOf(Navy, BrandBlue600)))
                .padding(horizontal = Spacing.lg, vertical = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Mensajes y Alertas",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = White,
                    modifier = Modifier.weight(1f)
                )
                // Status dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) Color.Green else Color.Red)
                )
            }
            Text(
                text = if (isConnected) "Conectado en tiempo real" else "Desconectado",
                style = MaterialTheme.typography.bodySmall,
                color = White.copy(alpha = 0.8f)
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (notifications.isEmpty()) {
                EmptyState(
                    title = "Bandeja de entrada vacía",
                    description = "Cuando recibas mensajes o alertas importantes, aparecerán aquí.",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Mail,
                            contentDescription = null,
                            tint = Gray500,
                            modifier = Modifier.padding(Spacing.sm)
                        )
                    },
                    modifier = Modifier.fillMaxSize().padding(Spacing.lg)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(key1 = notif.id) {
                            visible = true
                        }
                        androidx.compose.animation.AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { -40 }),
                            exit = fadeOut()
                        ) {
                            NotificationCard(
                                notification = notif,
                                onClick = { viewModel.markAsRead(notif.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val icon = when (notification.type.lowercase()) {
        "success" -> Icons.Outlined.CheckCircle
        "error" -> Icons.Outlined.ErrorOutline
        "warning" -> Icons.Outlined.Warning
        else -> Icons.Outlined.Info
    }
    
    val iconTint = when (notification.type.lowercase()) {
        "success" -> Color(0xFF10B981) // Emerald
        "error" -> Color(0xFFEF4444) // Red
        "warning" -> Color(0xFFF59E0B) // Amber
        else -> BrandBlue600
    }

    val df = SimpleDateFormat("HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) White else BrandBlue50 // Light blue background if unread
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
                    ),
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = df.format(Date(notification.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500
                )
            }
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(BrandBlue600)
                )
            }
        }
    }
}
