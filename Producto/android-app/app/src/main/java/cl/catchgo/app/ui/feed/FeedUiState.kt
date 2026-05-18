package cl.catchgo.app.ui.feed

import cl.catchgo.app.domain.model.JobCategory
import cl.catchgo.app.domain.model.JobOffer
import cl.catchgo.app.domain.model.JobApplication

enum class FeedTab {
    EXPLORAR, POSTULACIONES, PAGOS, POR_CALIFICAR, COMPLETADAS
}

data class FeedUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val offers: List<JobOffer> = emptyList(),
    val selectedCategory: JobCategory? = null,
    val searchQuery: String = "",
    val profileCompletion: Int = 0,
    val activeTab: FeedTab = FeedTab.EXPLORAR,
    val applications: List<JobApplication> = emptyList(),
    val filteredApplications: List<JobApplication> = emptyList(),
    val postulacionesCount: Int = 0,
    val pagosCount: Int = 0,
    val porCalificarCount: Int = 0,
    val completadasCount: Int = 0,
    val errorMessage: String? = null
)
