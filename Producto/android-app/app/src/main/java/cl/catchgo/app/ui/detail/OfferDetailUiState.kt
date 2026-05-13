package cl.catchgo.app.ui.detail

import cl.catchgo.app.domain.model.JobOffer

data class OfferDetailUiState(
    val isLoading: Boolean = true,
    val offer: JobOffer? = null,
    val errorMessage: String? = null,
    val isApplied: Boolean = false,
    val isApplying: Boolean = false,
    val applyError: String? = null
)
