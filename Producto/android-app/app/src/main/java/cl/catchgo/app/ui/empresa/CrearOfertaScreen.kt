package cl.catchgo.app.ui.empresa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.catchgo.app.ui.theme.BrandBlue700
import cl.catchgo.app.ui.theme.Spacing

private val CATEGORIAS = listOf(
    "Guardia", "Aseo / Limpieza", "Seguridad Eventos",
    "Garzón", "Cocina / Ayudante", "Anfitrión(a) / Recepcionista",
    "Carga/Descarga", "Reponedor", "Operario de Bodega", "Promotor(a)", "Delivery",
    "Profesor", "Tutor Particular", "Faenero", "Administrativo"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearOfertaScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: CrearOfertaViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var categoriaExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.success) {
        if (state.success) onBack()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Publicar Nuevo Turno") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Spacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(
                "Completa los detalles del turno",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            OutlinedTextField(
                value = state.titulo,
                onValueChange = viewModel::onTituloChange,
                label = { Text("Título del turno *") },
                placeholder = { Text("Ej. Guardia de Seguridad Turno Noche") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = categoriaExpanded,
                onExpandedChange = { categoriaExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.categoria,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoriaExpanded,
                    onDismissRequest = { categoriaExpanded = false }
                ) {
                    CATEGORIAS.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                viewModel.onCategoriaChange(cat)
                                categoriaExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.remuneracion,
                onValueChange = viewModel::onRemuneracionChange,
                label = { Text("Remuneración líquida ($) *") },
                placeholder = { Text("35000") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = state.fechaInicio,
                onValueChange = viewModel::onFechaInicioChange,
                label = { Text("Fecha de inicio * (YYYY-MM-DD)") },
                placeholder = { Text("2025-12-01") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.fechaFin,
                onValueChange = viewModel::onFechaFinChange,
                label = { Text("Fecha de fin (YYYY-MM-DD)") },
                placeholder = { Text("Opcional") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.ubicacion,
                onValueChange = viewModel::onUbicacionChange,
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.descripcion,
                onValueChange = viewModel::onDescripcionChange,
                label = { Text("Descripción y requisitos *") },
                placeholder = { Text("Describe el trabajo y los requisitos mínimos…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8
            )

            if (!state.error.isNullOrBlank()) {
                Text(state.error!!, color = Color(0xFFDC2626), style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(Spacing.sm))
            Button(
                onClick = viewModel::submit,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue700)
            ) {
                if (state.isLoading) CircularProgressIndicator(Modifier.height(Spacing.md), color = Color.White, strokeWidth = 2.dp)
                else Text("Publicar Oferta")
            }
        }
    }
}
