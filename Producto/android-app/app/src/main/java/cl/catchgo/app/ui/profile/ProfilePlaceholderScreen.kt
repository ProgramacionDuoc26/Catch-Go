package cl.catchgo.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.domain.model.UserSession
import cl.catchgo.app.ui.components.SecondaryButton
import cl.catchgo.app.ui.components.StatusBadge
import cl.catchgo.app.ui.components.StatusType
import cl.catchgo.app.ui.theme.BrandBlue50
import cl.catchgo.app.ui.theme.BrandBlue700
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Spacing

@Composable
fun ProfilePlaceholderScreen(
    session: UserSession,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Spacer(Modifier.height(Spacing.md))

        Surface(
            modifier = Modifier.size(72.dp),
            color = BrandBlue50,
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = session.user.fullName?.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = BrandBlue700
                )
            }
        }

        Text(
            text = session.user.fullName ?: session.user.email,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = session.user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500
        )

        StatusBadge(
            text = roleLabel(session.user.role),
            type = StatusType.Info
        )

        Spacer(Modifier.height(Spacing.md))

        Text(
            text = "Mi información",
            style = MaterialTheme.typography.titleSmall,
            color = Gray500
        )
        Text(
            text = "La edición de perfil llega en F9.",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )

        Spacer(Modifier.height(Spacing.lg))

        SecondaryButton(
            text = "Cerrar sesión",
            onClick = viewModel::onLogout,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun roleLabel(role: UserRole) = when (role) {
    UserRole.WORKER -> "Trabajador"
    UserRole.EMPRESA -> "Empresa"
    UserRole.UNKNOWN -> "Sin rol"
}
