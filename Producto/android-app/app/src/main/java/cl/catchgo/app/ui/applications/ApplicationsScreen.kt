package cl.catchgo.app.ui.applications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import cl.catchgo.app.core.util.timeAgo
import cl.catchgo.app.domain.model.ApplicationStatus
import cl.catchgo.app.domain.model.JobApplication
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.ui.applications.ApplicationsPlaceholderScreen
import cl.catchgo.app.ui.components.EmptyState
import cl.catchgo.app.ui.empresa.CandidatosScreen
import cl.catchgo.app.ui.components.StatusBadge
import cl.catchgo.app.ui.components.StatusType
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Gray700
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.Navy
import cl.catchgo.app.ui.theme.BrandBlue600
import cl.catchgo.app.ui.theme.White
import cl.catchgo.app.ui.theme.Gray200

@Composable
fun ApplicationsScreen(
    role: UserRole,
    onApplicationClick: (offerId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    when (role) {
        UserRole.WORKER -> WorkerApplicationsRoute(
            onApplicationClick = onApplicationClick,
            modifier = modifier
        )
        UserRole.EMPRESA -> CandidatosScreen(modifier = modifier)
        else -> ApplicationsPlaceholderScreen(role = role, modifier = modifier)
    }
}

@Composable
private fun WorkerApplicationsRoute(
    onApplicationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ApplicationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(modifier = modifier.fillMaxSize()) {
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
                text = "Mis Postulaciones",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = White
            )
            Text(
                text = "Sigue el estado de tus solicitudes en tiempo real.",
                style = MaterialTheme.typography.bodySmall,
                color = White.copy(alpha = 0.8f)
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.isEmpty -> EmptyApplications(modifier = Modifier.fillMaxSize())
                else -> ApplicationsList(
                    groups = state.groups,
                    onApplicationClick = onApplicationClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun ApplicationsList(
    groups: Map<ApplicationStatus, List<JobApplication>>,
    onApplicationClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        ApplicationStatus.entries.forEach { status ->
            val items = groups[status].orEmpty()
            if (items.isEmpty()) return@forEach
            item(key = "header-${status.name}") {
                SectionHeader(title = status.display, count = items.size)
            }
            items(items = items, key = { it.id }) { application ->
                ApplicationCard(
                    application = application,
                    onClick = { onApplicationClick(application.offerId) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Text(
        text = "$title · $count",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = Gray500,
        modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.xs)
    )
}

private val avatarColors = listOf(
    Color(0xFF0E7490) to Color(0xFFECFEFF),
    Color(0xFF1D4ED8) to Color(0xFFEFF6FF),
    Color(0xFF7C3AED) to Color(0xFFF5F3FF),
    Color(0xFF0F766E) to Color(0xFFF0FDFA),
    Color(0xFFB45309) to Color(0xFFFFFBEB),
    Color(0xFF9D174D) to Color(0xFFFFF1F2),
)

@Composable
private fun ApplicationCard(
    application: JobApplication,
    onClick: () -> Unit
) {
    val colorPair = avatarColors[application.company.hashCode().and(0x7FFFFFFF) % avatarColors.size]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Gray200),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colorPair.second),
                    contentAlignment = Alignment.Center
                ) {
                    if (!application.photoUrl.isNullOrBlank()) {
                        val mappedUrl = application.photoUrl
                            .replace("localhost", "10.0.2.2")
                            .replace("127.0.0.1", "10.0.2.2")
                        AsyncImage(
                            model = mappedUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = application.company.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorPair.first
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.offerTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${application.company} · ${application.comuna}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                }

                val display = when (application.rawStatus) {
                    "PENDIENTE" -> "Pendiente"
                    "ACEPTADO" -> "Aceptada"
                    "RECHAZADO" -> "Rechazada"
                    "TRABAJO_FINALIZADO" -> "Trabajo Finalizado"
                    "PAGO_ENVIADO" -> "Pago Enviado"
                    "PAGO_DISPUTADO" -> "Pago en Disputa"
                    "PAGO_CONFIRMADO" -> "Pago Confirmado"
                    "CALIFICADO_EMPRESA" -> "Calificada"
                    "CALIFICADO_TRABAJADOR" -> "Finalizada"
                    "FINALIZADA" -> "Finalizada"
                    "ARCHIVADA" -> "Archivada"
                    else -> application.rawStatus.lowercase().replaceFirstChar { it.uppercase() }
                }

                val type = when (application.rawStatus) {
                    "ACEPTADO", "PAGO_CONFIRMADO", "FINALIZADA", "CALIFICADO_TRABAJADOR" -> StatusType.Success
                    "RECHAZADO", "PAGO_DISPUTADO" -> StatusType.Error
                    else -> StatusType.Pending
                }

                StatusBadge(
                    text = display,
                    type = type
                )
            }
            Text(
                text = timeAgo(application.createdAtIso),
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
            if (!application.message.isNullOrBlank()) {
                Text(
                    text = "“${application.message}”",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray700
                )
            }
        }
    }
}

@Composable
private fun EmptyApplications(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyState(
            title = "Aún no tienes postulaciones",
            description = "Cuando postules a una oferta vas a verla acá con su estado actual.",
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Assignment,
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.padding(Spacing.sm)
                )
            }
        )
    }
}
