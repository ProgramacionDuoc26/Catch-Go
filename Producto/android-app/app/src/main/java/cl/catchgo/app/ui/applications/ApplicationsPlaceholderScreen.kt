package cl.catchgo.app.ui.applications

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.ui.components.EmptyState
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Spacing

@Composable
fun ApplicationsPlaceholderScreen(
    role: UserRole,
    modifier: Modifier = Modifier
) {
    val (title, description) = when (role) {
        UserRole.EMPRESA -> "Candidatos" to "Acá vas a revisar y aceptar candidatos para tus ofertas. Disponible en F8."
        else -> "Mis postulaciones" to "Acá vas a ver el estado de tus postulaciones. Disponible en F8."
    }
    EmptyState(
        title = title,
        description = description,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Assignment,
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.padding(Spacing.sm)
            )
        },
        modifier = modifier.fillMaxSize().padding(Spacing.lg)
    )
}
