package cl.catchgo.app.ui.feed

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.ui.components.EmptyState
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Spacing

@Composable
fun FeedPlaceholderScreen(
    role: UserRole,
    modifier: Modifier = Modifier
) {
    val (title, description) = when (role) {
        UserRole.EMPRESA -> "Mis ofertas" to "Acá vas a publicar y ver tus ofertas activas. Disponible en F6."
        else -> "Ofertas cerca tuyo" to "Acá vas a ver ofertas compatibles con tu perfil y ubicación. Disponible en F6."
    }
    EmptyState(
        title = title,
        description = description,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.padding(Spacing.sm)
            )
        },
        modifier = modifier.fillMaxSize().padding(Spacing.lg)
    )
}
