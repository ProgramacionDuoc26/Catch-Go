package cl.catchgo.app.ui.detail

import cl.catchgo.app.domain.model.JobApplication
import cl.catchgo.app.domain.model.JobOffer

data class OfferDetailUiState(
    val isLoading: Boolean = true,
    val offer: JobOffer? = null,
    val errorMessage: String? = null,
    val isApplied: Boolean = false,
    val activeApplication: JobApplication? = null,
    val isApplying: Boolean = false,
    val applyError: String? = null,
    val isActionLoading: Boolean = false,
    val actionError: String? = null
)
