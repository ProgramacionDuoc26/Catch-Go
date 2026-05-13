package cl.catchgo.app.ui.skills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.catchgo.app.domain.model.CategoriaHabilidades
import cl.catchgo.app.ui.components.PrimaryButton
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Gray900
import cl.catchgo.app.ui.theme.Teal500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsSetupScreen(
    onBack: () -> Unit,
    viewModel: SkillsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis habilidades") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(onClick = onBack) { Text("Omitir") }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Selecciona las habilidades que tienes. Puedes agregar las tuyas con el botón +",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )
            Spacer(Modifier.height(16.dp))

            state.categorias.forEach { categoria ->
                CategoriaSection(
                    categoria = categoria,
                    selectedIds = state.selectedIds,
                    onToggle = viewModel::toggleHabilidad,
                    onAddCustom = { nombre ->
                        viewModel.onCustomSkillInput(nombre)
                        viewModel.onCustomSkillCategoriaSelected(categoria.id)
                        viewModel.addCustomSkill()
                    }
                )
                Spacer(Modifier.height(16.dp))
            }

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }

            PrimaryButton(
                text = if (state.isSaving) "Guardando..." else "Guardar habilidades",
                onClick = { viewModel.guardar(onBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving && state.selectedIds.isNotEmpty()
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoriaSection(
    categoria: CategoriaHabilidades,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    onAddCustom: (String) -> Unit
) {
    var showInput by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    Column {
        Text(
            text = categoria.nombre,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Gray900
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categoria.habilidades.forEach { habilidad ->
                val selected = habilidad.id in selectedIds
                FilterChip(
                    selected = selected,
                    onClick = { onToggle(habilidad.id) },
                    label = { Text(habilidad.nombre, style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Outlined.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Teal500.copy(alpha = 0.15f),
                        selectedLabelColor = Teal500
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
            IconButton(
                onClick = { showInput = !showInput },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Agregar", tint = Teal500)
            }
        }

        if (showInput) {
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Nombre de la habilidad") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (inputText.isNotBlank()) {
                            onAddCustom(inputText.trim())
                            inputText = ""
                            showInput = false
                        }
                    }),
                    shape = RoundedCornerShape(12.dp)
                )
                TextButton(onClick = {
                    if (inputText.isNotBlank()) {
                        onAddCustom(inputText.trim())
                        inputText = ""
                        showInput = false
                    }
                }) { Text("Añadir") }
            }
        }
    }
}
