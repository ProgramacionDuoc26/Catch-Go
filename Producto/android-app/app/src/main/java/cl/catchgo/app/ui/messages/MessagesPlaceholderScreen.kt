package cl.catchgo.app.ui.messages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.catchgo.app.ui.components.EmptyState
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Spacing

@Composable
fun MessagesPlaceholderScreen(
    modifier: Modifier = Modifier
) {
    EmptyState(
        title = "Mensajes",
        description = "Cuando una empresa te acepte, vas a poder coordinar acá. Próximamente.",
        icon = {
            Icon(
                imageVector = Icons.Outlined.Mail,
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.padding(Spacing.sm)
            )
        },
        modifier = modifier.fillMaxSize().padding(Spacing.lg)
    )
}
