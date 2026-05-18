package cl.catchgo.app.ui.empresa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.catchgo.app.ui.components.EmptyState
import cl.catchgo.app.ui.components.MatchBadge
import cl.catchgo.app.ui.theme.BrandBlue600
import cl.catchgo.app.ui.theme.BrandBlue700
import cl.catchgo.app.ui.theme.Navy
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.White

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CandidatosScreen(modifier: Modifier = Modifier) {
    val viewModel: CandidatosViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    var tabIndex by remember { mutableIntStateOf(0) }
    var candidatoParaCalificar by remember { mutableStateOf<CandidatoItem?>(null) }

    val context = LocalContext.current
    LaunchedEffect(state.error) {
        state.error?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    val tabs = listOf("Nuevos", "En Proceso", "Por Calificar", "Historial")

    Column(modifier = modifier.fillMaxSize()) {
        CandidatosHeader()

        ScrollableTabRow(
            selectedTabIndex = tabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = Spacing.md
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = {
                        val count = state.candidatos.count { c ->
                            matchesTab(c.estado, index)
                        }
                        Text("$title ($count)", style = MaterialTheme.typography.labelMedium)
                    }
                )
            }
        }

        when {
            state.isLoading && state.candidatos.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandBlue600)
            }
            else -> {
                androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = viewModel::load,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val filtered = state.candidatos.filter { matchesTab(it.estado, tabIndex) }
                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                title = "Sin candidatos aquí",
                                description = "Las postulaciones aparecerán aquí en este estado.",
                                icon = {
                                    Icon(Icons.Outlined.Groups, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                }
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            items(filtered, key = { it.applicationId }) { candidato ->
                                CandidatoCard(
                                    candidato = candidato,
                                    onAceptar = { viewModel.updateStatus(candidato.applicationId, "ACEPTADO") },
                                    onRechazar = { viewModel.updateStatus(candidato.applicationId, "RECHAZADO") },
                                    onValidarTrabajo = { viewModel.updateStatus(candidato.applicationId, "TRABAJO_FINALIZADO") },
                                    onGenerarPago = { viewModel.updateStatus(candidato.applicationId, "PAGO_ENVIADO") },
                                    onCalificar = { candidatoParaCalificar = candidato }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    candidatoParaCalificar?.let { candidato ->
        RatingBottomSheet(
            nombre = candidato.nombre,
            onDismiss = { candidatoParaCalificar = null },
            onSubmit = { stars ->
                viewModel.submitRating(candidato, stars)
                candidatoParaCalificar = null
            }
        )
    }
}

private fun matchesTab(estado: String, tabIndex: Int): Boolean = when (tabIndex) {
    0 -> estado == "PENDIENTE"
    1 -> estado in listOf("ACEPTADO", "TRABAJO_FINALIZADO", "PAGO_ENVIADO")
    2 -> estado in listOf("PAGO_CONFIRMADO", "CALIFICADO_TRABAJADOR")
    3 -> estado in listOf("CALIFICADO_EMPRESA", "RECHAZADO", "FINALIZADA")
    else -> false
}

@Composable
private fun CandidatosHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Navy, BrandBlue600)))
            .padding(horizontal = Spacing.lg, vertical = Spacing.lg)
    ) {
        Text(
            "Gestión de Candidatos",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = White
        )
        Text(
            "Revisa y selecciona a los mejores trabajadores",
            style = MaterialTheme.typography.bodySmall,
            color = White.copy(alpha = 0.75f)
        )
    }
}

@Composable
private fun CandidatoCard(
    candidato: CandidatoItem,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onValidarTrabajo: () -> Unit,
    onGenerarPago: () -> Unit,
    onCalificar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        candidato.nombre,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Postula a: ${candidato.jobTitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                MatchBadge(score = candidato.matchScore)
            }

            if (candidato.descripcion.isNotBlank()) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    candidato.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 2
                )
            }

            Spacer(Modifier.height(Spacing.sm))

            EstadoBadge(candidato.estado)

            Spacer(Modifier.height(Spacing.sm))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                when (candidato.estado) {
                    "PENDIENTE" -> {
                        Button(
                            onClick = onAceptar,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue700),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.CheckCircle, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Aceptar")
                        }
                        OutlinedButton(
                            onClick = onRechazar,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                        ) {
                            Text("Rechazar")
                        }
                    }
                    "ACEPTADO" -> {
                        Button(
                            onClick = onValidarTrabajo,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0891B2))
                        ) {
                            Text("Validar Trabajo Realizado")
                        }
                    }
                    "TRABAJO_FINALIZADO" -> {
                        Button(
                            onClick = onGenerarPago,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Outlined.CheckCircle, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Generar Pago")
                        }
                    }
                    "PAGO_CONFIRMADO", "CALIFICADO_TRABAJADOR" -> {
                        Button(
                            onClick = onCalificar,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                        ) {
                            Icon(Icons.Outlined.Star, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Calificar Trabajador")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoBadge(estado: String) {
    val (label, color) = when (estado) {
        "PENDIENTE" -> "Pendiente" to Color(0xFFF59E0B)
        "ACEPTADO" -> "Seleccionado" to Color(0xFF2563EB)
        "TRABAJO_FINALIZADO" -> "Trabajo Realizado" to Color(0xFF0891B2)
        "PAGO_ENVIADO" -> "Pago Enviado" to Color(0xFF059669)
        "PAGO_CONFIRMADO" -> "Pago Confirmado" to Color(0xFF059669)
        "CALIFICADO_TRABAJADOR" -> "Te Calificaron" to Color(0xFF7C3AED)
        "CALIFICADO_EMPRESA", "FINALIZADA" -> "Completado" to Color(0xFF6B7280)
        "RECHAZADO" -> "Rechazado" to Color(0xFFDC2626)
        else -> estado to Color.Gray
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}
