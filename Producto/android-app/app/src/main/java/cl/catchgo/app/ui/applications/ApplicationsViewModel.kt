package cl.catchgo.app.ui.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.domain.model.ApplicationStatus
import cl.catchgo.app.domain.repository.ApplicationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplicationsViewModel @Inject constructor(
    private val applicationsRepository: ApplicationsRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            runCatching {
                applicationsRepository.refreshFromBackend()
            }
        }
    }

    val state: StateFlow<ApplicationsUiState> = applicationsRepository
        .observeMyApplications()
        .map { applications ->
            ApplicationsUiState(
                isLoading = false,
                groups = ApplicationStatus.entries.associateWith { status ->
                    applications.filter { it.status == status }
                }
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ApplicationsUiState(isLoading = true)
        )
}
