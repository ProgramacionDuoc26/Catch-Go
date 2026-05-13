package cl.catchgo.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.domain.model.HabilidadUsuario
import cl.catchgo.app.domain.model.RadarData
import cl.catchgo.app.domain.repository.AuthRepository
import cl.catchgo.app.domain.repository.HabilidadesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val radar: RadarData? = null,
    val habilidades: List<HabilidadUsuario> = emptyList()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val habilidadesRepository: HabilidadesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadSkills(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val radar = habilidadesRepository.getRadar(userId).getOrNull()
            val skills = habilidadesRepository.getHabilidadesUsuario(userId).getOrDefault(emptyList())
            _uiState.update { it.copy(isLoading = false, radar = radar, habilidades = skills) }
        }
    }

    fun onLogout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
