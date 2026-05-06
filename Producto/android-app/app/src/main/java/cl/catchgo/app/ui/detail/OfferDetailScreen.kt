package cl.catchgo.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.catchgo.app.domain.model.JobOffer
import cl.catchgo.app.ui.apply.ApplyBottomSheet
import cl.catchgo.app.ui.components.EmptyState
import cl.catchgo.app.ui.components.PrimaryButton
import cl.catchgo.app.ui.components.SecondaryButton
import cl.catchgo.app.ui.components.StatusBadge
import cl.catchgo.app.ui.components.StatusType
import cl.catchgo.app.ui.theme.Gray100
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Gray700
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.White
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OfferDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                DetailEvent.ApplicationSucceeded -> {
                    showSheet = false
                    snackbarHostState.showSnackbar("Postulación enviada")
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Detalle de oferta", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Gray700,
                    navigationIconContentColor = Gray700
                )
            )
        },
        bottomBar = {
            val offer = state.offer
            if (offer != null) {
                Surface(color = White, shadowElevation = 0.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                    ) {
                        if (state.isApplied) {
                            SecondaryButton(
                                text = "Ya postulaste",
                                onClick = {},
                                enabled = false
                            )
                        } else {
                            PrimaryButton(
                                text = "Postular a esta oferta",
                                onClick = { showSheet = true }
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = White
    ) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.errorMessage != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = "No pudimos cargar la oferta",
                    description = state.errorMessage ?: "",
                    action = {
                        PrimaryButton(
                            text = "Reintentar",
                            onClick = viewModel::retry,
                            fullWidth = false
                        )
                    }
                )
            }

            state.offer != null -> OfferDetailContent(
                offer = state.offer!!,
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showSheet && state.offer != null) {
        ApplyBottomSheet(
            offerTitle = state.offer!!.title,
            company = state.offer!!.company,
            isSubmitting = state.isApplying,
            errorMessage = state.applyError,
            onConfirm = { msg -> viewModel.submitApplication(msg) },
            onDismiss = {
                if (!state.isApplying) {
                    viewModel.clearApplyError()
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                }
            },
            sheetState = sheetState
        )
    }
}

@Composable
private fun OfferDetailContent(offer: JobOffer, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Header(offer)
        SectionTitle("Sobre el trabajo")
        Text(
            text = offer.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray700
        )

        SectionTitle("Detalles")
        DetailRow(label = "Ubicación", value = "${offer.comuna}, ${offer.region}")
        DetailRow(label = "Jornada", value = offer.jornada)
        DetailRow(label = "Horario", value = offer.scheduleText)
        DetailRow(label = "Remuneración", value = formatSalary(offer.salaryClp, offer.salaryUnit))

        if (offer.requirements.isNotEmpty()) {
            SectionTitle("Requisitos")
            RequirementChips(offer.requirements)
        }

        Spacer(Modifier.height(Spacing.md))
    }
}

@Composable
private fun Header(offer: JobOffer) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = offer.company,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray500
                )
            }
            ScoreBadge(offer.score)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            StatusBadge(text = offer.category.display, type = StatusType.Info)
            StatusBadge(text = offer.comuna, type = StatusType.Neutral)
        }
    }
}

@Composable
private fun ScoreBadge(score: Int) {
    val type = when {
        score >= 80 -> StatusType.Success
        score >= 50 -> StatusType.Pending
        else -> StatusType.Neutral
    }
    StatusBadge(text = "$score% match", type = type)
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = Gray700,
        modifier = Modifier.padding(top = Spacing.sm)
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray700,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun RequirementChips(requirements: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        requirements.forEach { requirement ->
            Surface(
                color = Gray100,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "• $requirement",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700,
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
                )
            }
        }
    }
}

private fun formatSalary(clp: Int, unit: String): String {
    val formatted = "%,d".format(clp).replace(",", ".")
    return "\$$formatted CLP / $unit"
}
