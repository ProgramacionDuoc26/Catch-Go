package cl.catchgo.app.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.catchgo.app.ui.components.CatchTextField
import cl.catchgo.app.ui.components.PrimaryButton
import cl.catchgo.app.ui.theme.CatchGoTheme
import cl.catchgo.app.ui.theme.Error600
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Spacing

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::onSubmit,
        modifier = modifier
    )
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Spacer(Modifier.height(Spacing.xl))

        Text(text = "Catch-Go", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "Encuentra trabajos cerca tuyo.",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500
        )

        Spacer(Modifier.height(Spacing.lg))

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
            text = "Iniciar sesión",
            onClick = onSubmit,
            enabled = state.canSubmit,
            loading = state.isLoading
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun LoginScreenPreview() {
    CatchGoTheme {
        LoginContent(
            state = LoginUiState(email = "marco@correo.cl"),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, name = "Login con error")
@Composable
private fun LoginScreenErrorPreview() {
    CatchGoTheme {
        LoginContent(
            state = LoginUiState(
                email = "marco@correo.cl",
                password = "1234",
                errorMessage = "Credenciales inválidas"
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {}
        )
    }
}
