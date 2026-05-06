package cl.catchgo.app.ui.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.ui.components.CatchTextField
import cl.catchgo.app.ui.components.PrimaryButton
import cl.catchgo.app.ui.theme.CatchGoTheme
import cl.catchgo.app.ui.theme.Error600
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Spacing

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RegisterContent(
        state = state,
        onRoleSelect = viewModel::onRoleSelect,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onFullNameChange = viewModel::onFullNameChange,
        onRutChange = viewModel::onRutChange,
        onPhoneChange = viewModel::onPhoneChange,
        onSubmit = viewModel::onSubmit,
        onLoginClick = onLoginClick,
        modifier = modifier
    )
}

@Composable
private fun RegisterContent(
    state: RegisterUiState,
    onRoleSelect: (UserRole) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onFullNameChange: (String) -> Unit,
    onRutChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Spacer(Modifier.height(Spacing.md))

        Text(text = "Crear cuenta", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "Empezá en menos de un minuto.",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500
        )

        Spacer(Modifier.height(Spacing.md))

        Text(
            text = "¿Cómo vas a usar Catch-Go?",
            style = MaterialTheme.typography.titleSmall,
            color = Gray500
        )
        RoleSelector(selected = state.role, onSelect = onRoleSelect)

        Spacer(Modifier.height(Spacing.sm))

        CatchTextField(
            value = state.fullName,
            onValueChange = onFullNameChange,
            label = if (state.role == UserRole.EMPRESA) "Razón social" else "Nombre completo",
            enabled = !state.isLoading,
            isError = state.errorMessage != null
        )
        CatchTextField(
            value = state.rut,
            onValueChange = onRutChange,
            label = "RUT",
            placeholder = "12.345.678-9",
            enabled = !state.isLoading,
            isError = state.errorMessage != null
        )
        CatchTextField(
            value = state.phone,
            onValueChange = onPhoneChange,
            label = "Teléfono",
            placeholder = "+56 9 1234 5678",
            keyboardType = KeyboardType.Phone,
            enabled = !state.isLoading,
            isError = state.errorMessage != null
        )
        CatchTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = "Email",
            placeholder = "tu@correo.cl",
            keyboardType = KeyboardType.Email,
            enabled = !state.isLoading,
            isError = state.errorMessage != null
        )
        CatchTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = "Contraseña",
            supportingText = "Mínimo 6 caracteres",
            isPassword = true,
            enabled = !state.isLoading,
            isError = state.errorMessage != null
        )

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Error600
            )
        }

        Spacer(Modifier.height(Spacing.sm))

        PrimaryButton(
            text = "Crear cuenta y empezar",
            onClick = onSubmit,
            enabled = state.canSubmit,
            loading = state.isLoading
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿Ya tienes cuenta?",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )
            TextButton(onClick = onLoginClick, enabled = !state.isLoading) {
                Text(text = "Inicia sesión")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun RegisterScreenPreview() {
    CatchGoTheme {
        RegisterContent(
            state = RegisterUiState(role = UserRole.WORKER, email = "marco@correo.cl"),
            onRoleSelect = {},
            onEmailChange = {},
            onPasswordChange = {},
            onFullNameChange = {},
            onRutChange = {},
            onPhoneChange = {},
            onSubmit = {},
            onLoginClick = {}
        )
    }
}
