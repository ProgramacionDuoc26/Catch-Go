package cl.catchgo.app.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.catchgo.app.ui.components.EmptyState
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.Navy
import cl.catchgo.app.ui.theme.BrandBlue600
import cl.catchgo.app.ui.theme.White

@Composable
fun MessagesPlaceholderScreen(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Beautiful Blue Gradient Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Navy, BrandBlue600)
                    )
                )
                .padding(horizontal = Spacing.lg, vertical = 24.dp)
        ) {
            Text(
                text = "Mensajes",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = White
            )
            Text(
                text = "Coordinación y chat directo con las empresas.",
                style = MaterialTheme.typography.bodySmall,
                color = White.copy(alpha = 0.8f)
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            EmptyState(
                title = "Bandeja de entrada vacía",
                description = "Cuando una empresa te acepte, vas a poder coordinar acá. Próximamente.",
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
        }
    }
}
