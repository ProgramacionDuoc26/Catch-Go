package cl.catchgo.app.ui.skills

import cl.catchgo.app.domain.model.CategoriaHabilidades
import cl.catchgo.app.domain.model.HabilidadUsuario

data class SkillsUiState(
    val isLoading: Boolean = false,
    val categorias: List<CategoriaHabilidades> = emptyList(),
    val misHabilidades: List<HabilidadUsuario> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val customSkillInput: String = "",
    val customSkillCategoriaId: Long? = null
)
