package cl.catchgo.app.ui.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.data.local.SessionStore
import cl.catchgo.app.domain.repository.HabilidadesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SkillsViewModel @Inject constructor(
    private val habilidadesRepository: HabilidadesRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _state = MutableStateFlow(SkillsUiState())
    val state: StateFlow<SkillsUiState> = _state.asStateFlow()

    private var userId: Long = 0L

    init {
        viewModelScope.launch {
            userId = sessionStore.session.first()?.user?.id?.toLongOrNull() ?: 0L
            load()
        }
    }

    private suspend fun load() {
        _state.update { it.copy(isLoading = true) }
        val cats = habilidadesRepository.getCategorias().getOrDefault(emptyList())
        val mis = if (userId > 0) habilidadesRepository.getHabilidadesUsuario(userId).getOrDefault(emptyList()) else emptyList()
        val selectedIds = mis.map { it.habilidadId }.toSet()
        _state.update { it.copy(isLoading = false, categorias = cats, misHabilidades = mis, selectedIds = selectedIds) }
    }

    fun toggleHabilidad(habilidadId: Long) {
        _state.update { s ->
            val ids = if (habilidadId in s.selectedIds) s.selectedIds - habilidadId
                      else s.selectedIds + habilidadId
            s.copy(selectedIds = ids)
        }
    }

    fun onCustomSkillInput(text: String) = _state.update { it.copy(customSkillInput = text) }

    fun onCustomSkillCategoriaSelected(categoriaId: Long) =
        _state.update { it.copy(customSkillCategoriaId = categoriaId) }

    fun addCustomSkill() {
        val nombre = _state.value.customSkillInput.trim()
        val catId = _state.value.customSkillCategoriaId ?: return
        if (nombre.isBlank() || userId == 0L) return
        viewModelScope.launch {
            habilidadesRepository.crearHabilidad(nombre, catId, userId)
                .onSuccess { nueva ->
                    _state.update { s ->
                        val cats = s.categorias.map { cat ->
                            if (cat.id == catId) cat.copy(habilidades = cat.habilidades + nueva) else cat
                        }
                        s.copy(
                            categorias = cats,
                            selectedIds = s.selectedIds + nueva.id,
                            customSkillInput = "",
                            customSkillCategoriaId = null
                        )
                    }
                }
                .onFailure { e -> _state.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun guardar(onDone: () -> Unit) {
        if (userId == 0L) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            val selected = _state.value.selectedIds
            val current = _state.value.misHabilidades.map { it.habilidadId }.toSet()
            val toAdd = selected - current
            var failed = false
            toAdd.forEach { habilidadId ->
                habilidadesRepository.asignarHabilidad(userId, habilidadId, 50)
                    .onFailure { failed = true }
            }
            _state.update { it.copy(isSaving = false, errorMessage = if (failed) "Error al guardar algunas habilidades" else null) }
            if (!failed) onDone()
        }
    }
}
