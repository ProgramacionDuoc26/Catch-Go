package cl.catchgo.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.domain.model.UserSession
import cl.catchgo.app.ui.components.SecondaryButton
import cl.catchgo.app.ui.components.StatusBadge
import cl.catchgo.app.ui.components.StatusType
import cl.catchgo.app.ui.theme.BrandBlue600
import cl.catchgo.app.ui.theme.Gray100
import cl.catchgo.app.ui.theme.Gray200
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Gray700
import cl.catchgo.app.ui.theme.Gray900
import cl.catchgo.app.ui.theme.Navy
import cl.catchgo.app.ui.theme.NavyDeep
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.Teal200
import cl.catchgo.app.ui.theme.Teal500
import cl.catchgo.app.ui.theme.Teal50
import cl.catchgo.app.ui.theme.Teal700
import cl.catchgo.app.ui.theme.White

@Composable
fun ProfilePlaceholderScreen(
    session: UserSession,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(session)

        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Spacer(Modifier.height(Spacing.sm))

            InfoSection(session)

            HorizontalDivider(color = Gray200)

            Spacer(Modifier.height(Spacing.sm))

            SecondaryButton(
                text = "Cerrar sesión",
                onClick = viewModel::onLogout,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun ProfileHeader(session: UserSession) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(colors = listOf(NavyDeep, Navy, BrandBlue600))
            )
            .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Teal500.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = session.user.fullName?.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = White
                )
            }

            Spacer(Modifier.height(Spacing.md))

            Text(
                text = session.user.fullName ?: session.user.email,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = White
            )
            Text(
                text = session.user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(Spacing.sm))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Teal500.copy(alpha = 0.25f))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = roleLabel(session.user.role),
                    style = MaterialTheme.typography.labelMedium,
                    color = Teal200
                )
            }
        }
    }
}

@Composable
private fun InfoSection(session: UserSession) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Gray100,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = "Información de cuenta",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Gray900
            )
            Spacer(Modifier.height(Spacing.sm))
            InfoRow(icon = Icons.Outlined.AlternateEmail, label = "Email", value = session.user.email)
            if (session.user.fullName != null) {
                InfoRow(icon = Icons.Outlined.Badge, label = "Nombre", value = session.user.fullName)
            }
            InfoRow(icon = Icons.Outlined.WorkOutline, label = "Rol", value = roleLabel(session.user.role))
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Teal500, modifier = Modifier.size(18.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Gray500)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Gray900)
        }
    }
}

private fun roleLabel(role: UserRole) = when (role) {
    UserRole.WORKER -> "Trabajador"
    UserRole.EMPRESA -> "Empresa"
    UserRole.UNKNOWN -> "Sin rol"
}
