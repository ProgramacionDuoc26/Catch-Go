package cl.catchgo.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.catchgo.app.ui.components.CatchTextField
import cl.catchgo.app.ui.components.PrimaryButton
import cl.catchgo.app.ui.theme.BrandBlue600
import cl.catchgo.app.ui.theme.CatchGoTheme
import cl.catchgo.app.ui.theme.Error600
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Navy
import cl.catchgo.app.ui.theme.NavyDeep
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.Teal500
import cl.catchgo.app.ui.theme.White

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::onSubmit,
        onRegisterClick = onRegisterClick,
        modifier = modifier
    )
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(NavyDeep, Navy, BrandBlue600)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier
                        .size(180.dp, 80.dp)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = White,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = cl.catchgo.app.R.drawable.logo),
                            contentDescription = "Catch & Go Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Conectamos trabajo con talento",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFBAE6FD)
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = White,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(Modifier.height(Spacing.xs))

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
                    text = "Entrar",
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
                        text = "¿No tienes cuenta?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                    TextButton(onClick = onRegisterClick, enabled = !state.isLoading) {
                        Text(text = "Registrarse")
                    }
                }
            }
        }
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
            onSubmit = {},
            onRegisterClick = {}
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
            onSubmit = {},
            onRegisterClick = {}
        )
    }
}
