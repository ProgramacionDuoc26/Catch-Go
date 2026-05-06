package cl.catchgo.app.ui.feed

import cl.catchgo.app.domain.model.JobCategory
import cl.catchgo.app.domain.model.JobOffer

data class FeedUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val offers: List<JobOffer> = emptyList(),
    val selectedCategory: JobCategory? = null,
    val errorMessage: String? = null
)
