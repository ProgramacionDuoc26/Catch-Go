package cl.catchgo.app.ui.applications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    when {
        state.isLoading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.isEmpty -> EmptyApplications(modifier = modifier)
        else -> ApplicationsList(
            groups = state.groups,
            onApplicationClick = onApplicationClick,
            modifier = modifier
        )
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

@Composable
private fun ApplicationCard(
    application: JobApplication,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.offerTitle,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${application.company} · ${application.comuna}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                }
                StatusBadge(
                    text = application.status.display,
                    type = when (application.status) {
                        ApplicationStatus.PENDING -> StatusType.Pending
                        ApplicationStatus.ACCEPTED -> StatusType.Success
                        ApplicationStatus.REJECTED -> StatusType.Error
                    }
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
