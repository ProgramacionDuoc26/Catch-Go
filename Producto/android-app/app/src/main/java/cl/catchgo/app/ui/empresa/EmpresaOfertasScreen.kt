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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.catchgo.app.data.remote.dto.JobOfferDto
import cl.catchgo.app.ui.components.EmptyState
import cl.catchgo.app.ui.theme.BrandBlue600
import cl.catchgo.app.ui.theme.BrandBlue700
import cl.catchgo.app.ui.theme.Navy
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpresaOfertasScreen(
    onCrearOferta: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: EmpresaOfertasViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCrearOferta,
                containerColor = BrandBlue700,
                contentColor = White,
                shape = CircleShape
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Nueva oferta")
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = viewModel::load,
            modifier = Modifier.padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                EmpresaOfertasHeader(count = state.ofertas.size)

                when {
                    state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandBlue600)
                    }
                    state.ofertas.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            title = "Sin ofertas publicadas",
                            description = "Pulsa + para publicar tu primer turno.",
                            icon = {
                                Icon(Icons.Outlined.Work, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            }
                        )
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        items(state.ofertas, key = { it.id }) { oferta ->
                            OfertaEmpresaCard(
                                oferta = oferta,
                                onDelete = { viewModel.delete(oferta.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmpresaOfertasHeader(count: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Navy, BrandBlue600)))
            .padding(horizontal = Spacing.lg, vertical = Spacing.lg)
    ) {
        Text(
            "Mis Ofertas",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = White
        )
        Text(
            if (count == 1) "1 turno publicado" else "$count turnos publicados",
            style = MaterialTheme.typography.bodySmall,
            color = White.copy(alpha = 0.75f)
        )
    }
}

@Composable
private fun OfertaEmpresaCard(
    oferta: JobOfferDto,
    onDelete: () -> Unit
) {
    val estadoColor = when (oferta.estado?.uppercase()) {
        "ABIERTA" -> Color(0xFF059669)
        "CERRADA" -> Color(0xFF6B7280)
        "CON_CANDIDATOS" -> Color(0xFF2563EB)
        "COMPLETADA" -> Color(0xFF7C3AED)
        else -> Color(0xFF6B7280)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        oferta.titulo ?: "Sin título",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        oferta.ubicacion ?: "Sin ubicación",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, null, tint = Color(0xFFDC2626))
                }
            }

            Spacer(Modifier.height(Spacing.xs))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Box(
                    modifier = Modifier
                        .background(estadoColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        oferta.estado ?: "ABIERTA",
                        style = MaterialTheme.typography.labelSmall,
                        color = estadoColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    "\$${"%,d".format(oferta.remuneracion ?: 0).replace(",", ".")}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = BrandBlue700
                )
            }

            if (!oferta.fechaInicio.isNullOrBlank()) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Inicio: ${oferta.fechaInicio}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
