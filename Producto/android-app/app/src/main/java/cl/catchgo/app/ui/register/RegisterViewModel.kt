package cl.catchgo.app.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.core.error.ErrorMapper
import cl.catchgo.app.domain.model.RegisterInput
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onRoleSelect(role: UserRole) = _state.update { it.copy(role = role, errorMessage = null) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = v, errorMessage = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, errorMessage = null) }
    fun onFullNameChange(v: String) = _state.update { it.copy(fullName = v, errorMessage = null) }
    fun onRutChange(v: String) = _state.update { it.copy(rut = v, errorMessage = null) }
    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v, errorMessage = null) }

    fun onSubmit() {
        val s = _state.value
        if (!s.canSubmit) return
        val role = s.role ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.register(
                RegisterInput(
                    email = s.email.trim(),
                    password = s.password,
                    fullName = s.fullName.trim(),
                    rut = s.rut.trim(),
                    phone = s.phone.trim(),
                    role = role
                )
            ).onSuccess { _state.update { it.copy(isLoading = false) } }
                .onFailure { e ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = ErrorMapper.map(e).message)
                    }
                }
        }
    }
}
