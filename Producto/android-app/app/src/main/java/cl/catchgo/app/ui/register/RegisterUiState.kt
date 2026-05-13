package cl.catchgo.app.ui.register

import cl.catchgo.app.domain.model.UserRole

data class RegisterUiState(
    val role: UserRole? = null,
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val rut: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val canSubmit: Boolean
        get() = role != null &&
            email.isNotBlank() &&
            password.length >= 6 &&
            fullName.isNotBlank() &&
            rut.isNotBlank() &&
            phone.isNotBlank() &&
            !isLoading
}
