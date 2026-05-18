package cl.catchgo.app.ui.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.ui.theme.BrandBlue50
import cl.catchgo.app.ui.theme.BrandBlue700
import cl.catchgo.app.ui.theme.Gray300
import cl.catchgo.app.ui.theme.Gray700
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.White

@Composable
fun RoleSelector(
    selected: UserRole?,
    onSelect: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        RoleCard(
            role = UserRole.WORKER,
            label = "Soy trabajador",
            description = "Busco trabajos ocasionales",
            icon = Icons.Outlined.Work,
            selected = selected == UserRole.WORKER,
            onClick = { onSelect(UserRole.WORKER) },
            modifier = Modifier.weight(1f)
        )
        RoleCard(
            role = UserRole.EMPRESA,
            label = "Soy empresa",
            description = "Necesito cubrir turnos",
            icon = Icons.Outlined.Business,
            selected = selected == UserRole.EMPRESA,
            onClick = { onSelect(UserRole.EMPRESA) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RoleCard(
    role: UserRole,
    label: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val borderWidth = if (selected) 2.dp else 1.dp
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
    val accent = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(borderWidth, borderColor),
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = accent
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = accent
            )
        }
    }
}
