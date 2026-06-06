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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
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
    
    var showTermsDialog by remember { mutableStateOf(false) }
    var localErrorMessage by remember { mutableStateOf<String?>(null) }

    RegisterContent(
        state = state.copy(errorMessage = localErrorMessage ?: state.errorMessage),
        onRoleSelect = {
            localErrorMessage = null
            viewModel.onRoleSelect(it)
        },
        onEmailChange = {
            localErrorMessage = null
            viewModel.onEmailChange(it)
        },
        onPasswordChange = {
            localErrorMessage = null
            viewModel.onPasswordChange(it)
        },
        onFullNameChange = {
            localErrorMessage = null
            viewModel.onFullNameChange(it)
        },
        onRutChange = {
            localErrorMessage = null
            viewModel.onRutChange(it)
        },
        onPhoneChange = {
            localErrorMessage = null
            viewModel.onPhoneChange(it)
        },
        onSubmit = {
            localErrorMessage = null
            showTermsDialog = true
        },
        onLoginClick = onLoginClick,
        modifier = modifier
    )

    if (showTermsDialog && state.role != null) {
        TermsAndConditionsDialog(
            role = state.role!!,
            onAccept = {
                showTermsDialog = false
                viewModel.onSubmit()
            },
            onReject = {
                showTermsDialog = false
                localErrorMessage = "Solicitud rechazada: Debe aceptar los términos y condiciones de privacidad (Ley 19.628) para registrarse."
            },
            onDismiss = {
                showTermsDialog = false
            }
        )
    }
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

@Composable
private fun TermsAndConditionsDialog(
    role: UserRole,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            Column(
                modifier = Modifier
                    .padding(Spacing.lg)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = "Términos y Privacidad (Ley 19.628)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 280.dp)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = if (role == UserRole.WORKER) {
                            "Términos y Condiciones para Trabajadores\n\nEn conformidad con la Ley Nº 19.628 sobre Protección de la Vida Privada en Chile, al presionar 'Aceptar', usted otorga su consentimiento expreso para que Catch-Go recolecte, almacene y trate sus datos personales (nombre, RUT, correo electrónico, teléfono y calificaciones).\n\nEstos datos se utilizarán para gestionar su perfil, recomendarle ofertas de turnos y coordinar la realización de los mismos. Sus datos profesionales y de contacto serán compartidos única y exclusivamente con las Empresas organizadoras de los turnos a los que usted postule formalmente.\n\nUsted puede revocar este consentimiento o ejercer sus derechos de acceso, rectificación, cancelación y oposición escribiendo a soporte@catchgo.cl.\n\nSi no acepta estos términos, su solicitud de registro será rechazada."
                        } else {
                            "Términos y Condiciones para Empresas\n\nEn conformidad con la Ley Nº 19.628 sobre Protección de la Vida Privada en Chile, al presionar 'Aceptar', la Empresa acepta las condiciones de tratamiento seguro y confidencial de datos de la plataforma Catch-Go.\n\nLa Empresa se compromete a resguardar con estricta reserva toda información personal o datos sensibles de los Trabajadores a los que acceda. Queda estrictamente prohibido utilizar estos datos para fines ajenos a la cobertura de los turnos solicitados a través de Catch-Go.\n\nAmbas partes asumen plena responsabilidad legal por el cumplimiento de la Ley 19.628 respecto a la privacidad de los datos personales tratados en la plataforma.\n\nSi no acepta estos términos, su solicitud de registro será rechazada."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.End)
                ) {
                    OutlinedButton(onClick = onReject) {
                        Text("Rechazar")
                    }
                    Button(onClick = onAccept) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}
