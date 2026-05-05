package cl.catchgo.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import cl.catchgo.app.domain.model.UserSession
import cl.catchgo.app.ui.components.SecondaryButton
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Spacing

@Composable
fun HomePlaceholderScreen(
    session: UserSession,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Spacer(Modifier.height(Spacing.xl))

        Text(
            text = "Hola, ${session.user.fullName ?: session.user.email}",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Sesión iniciada como ${session.user.role.name.lowercase()}.",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500
        )
        Text(
            text = "El feed de ofertas llega en F6.",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )

        Spacer(Modifier.height(Spacing.lg))

        SecondaryButton(
            text = "Cerrar sesión",
            onClick = viewModel::onLogout
        )
    }
}
