package cl.catchgo.app.ui.empresa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.data.local.SessionStore
import cl.catchgo.app.data.remote.JobsApi
import cl.catchgo.app.data.remote.dto.JobOfferDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmpresaOfertasUiState(
    val isLoading: Boolean = true,
    val ofertas: List<JobOfferDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class EmpresaOfertasViewModel @Inject constructor(
    private val jobsApi: JobsApi,
    private val sessionStore: SessionStore
) : ViewModel() {

    val state: StateFlow<EmpresaOfertasUiState> get() = _state
    private val _state = MutableStateFlow(EmpresaOfertasUiState())

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val session = sessionStore.session.first() ?: return@launch
                val all = jobsApi.list()
                val mine = all.filter { it.empresaId == session.user.id }
                _state.update { it.copy(isLoading = false, ofertas = mine) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            runCatching { jobsApi.delete(id) }
            _state.update { it.copy(ofertas = it.ofertas.filter { o -> o.id != id }) }
        }
    }
}
