package cl.catchgo.app.ui.applications

import cl.catchgo.app.domain.model.ApplicationStatus
import cl.catchgo.app.domain.model.JobApplication

data class ApplicationsUiState(
    val isLoading: Boolean = true,
    val groups: Map<ApplicationStatus, List<JobApplication>> = emptyMap()
) {
    val isEmpty: Boolean get() = !isLoading && groups.values.all { it.isEmpty() }
}
