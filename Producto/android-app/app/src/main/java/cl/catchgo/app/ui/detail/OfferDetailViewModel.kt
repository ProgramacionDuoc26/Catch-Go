package cl.catchgo.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.core.error.ErrorMapper
import cl.catchgo.app.domain.repository.ApplicationsRepository
import cl.catchgo.app.domain.repository.JobsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfferDetailViewModel @Inject constructor(
    private val jobsRepository: JobsRepository,
    private val applicationsRepository: ApplicationsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val offerId: String = checkNotNull(savedStateHandle["id"]) {
        "OfferDetailViewModel requires 'id' nav argument"
    }

    private val internalState = MutableStateFlow(OfferDetailUiState())

    val state: StateFlow<OfferDetailUiState> = combine(
        internalState,
        applicationsRepository.observeMyApplications()
    ) { ui, apps ->
        val activeApp = apps.find { it.offerId == offerId }
        ui.copy(
            isApplied = activeApp != null,
            activeApplication = activeApp
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OfferDetailUiState())

    private val _events = Channel<DetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    fun retry() = load()

    fun submitApplication(message: String?) {
        viewModelScope.launch {
            internalState.update { it.copy(isApplying = true, applyError = null) }
            applicationsRepository.apply(offerId, message)
                .onSuccess {
                    internalState.update { it.copy(isApplying = false) }
                    _events.send(DetailEvent.ApplicationSucceeded)
                }
                .onFailure { e ->
                    internalState.update {
                        it.copy(isApplying = false, applyError = ErrorMapper.map(e).message)
                    }
                }
        }
    }

    fun cancelPostulation(applicationId: Long) {
        viewModelScope.launch {
            internalState.update { it.copy(isActionLoading = true, actionError = null) }
            applicationsRepository.cancelApplication(applicationId)
                .onSuccess {
                    internalState.update { it.copy(isActionLoading = false) }
                    _events.send(DetailEvent.StatusUpdated("Postulación cancelada con éxito"))
                }
                .onFailure { e ->
                    internalState.update {
                        it.copy(isActionLoading = false, actionError = ErrorMapper.map(e).message)
                    }
                }
        }
    }

    fun updateApplicationStatus(applicationId: Long, newStatus: String) {
        viewModelScope.launch {
            internalState.update { it.copy(isActionLoading = true, actionError = null) }
            applicationsRepository.updateStatus(applicationId, newStatus)
                .onSuccess {
                    internalState.update { it.copy(isActionLoading = false) }
                    _events.send(DetailEvent.StatusUpdated("Estado actualizado a $newStatus"))
                }
                .onFailure { e ->
                    internalState.update {
                        it.copy(isActionLoading = false, actionError = ErrorMapper.map(e).message)
                    }
                }
        }
    }

    fun clearApplyError() {
        internalState.update { it.copy(applyError = null) }
    }

    private fun load() {
        viewModelScope.launch {
            internalState.update { it.copy(isLoading = true, errorMessage = null) }
            jobsRepository.getOffer(offerId)
                .onSuccess { offer ->
                    internalState.update { it.copy(isLoading = false, offer = offer) }
                }
                .onFailure { e ->
                    internalState.update {
                        it.copy(isLoading = false, errorMessage = ErrorMapper.map(e).message)
                    }
                }
        }
    }
}

sealed interface DetailEvent {
    data object ApplicationSucceeded : DetailEvent
    data class StatusUpdated(val message: String) : DetailEvent
}
