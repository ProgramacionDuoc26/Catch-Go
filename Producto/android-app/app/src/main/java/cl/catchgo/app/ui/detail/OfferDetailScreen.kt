package cl.catchgo.app.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.catchgo.app.domain.model.JobOffer
import cl.catchgo.app.ui.apply.ApplyBottomSheet
import cl.catchgo.app.ui.components.EmptyState
import cl.catchgo.app.ui.components.PrimaryButton
import cl.catchgo.app.ui.components.SecondaryButton
import cl.catchgo.app.ui.components.StatusBadge
import cl.catchgo.app.ui.components.StatusType
import cl.catchgo.app.ui.theme.BrandBlue50
import cl.catchgo.app.ui.theme.BrandBlue700
import cl.catchgo.app.ui.theme.Gray100
import cl.catchgo.app.ui.theme.Gray200
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Gray700
import cl.catchgo.app.ui.theme.Gray900
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.Teal500
import cl.catchgo.app.ui.theme.Teal50
import cl.catchgo.app.ui.theme.Teal700
import cl.catchgo.app.ui.theme.White
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
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
    var showDistanceDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showDisputeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                DetailEvent.ApplicationSucceeded -> {
                    showSheet = false
                    snackbarHostState.showSnackbar("Postulación enviada")
                }
                is DetailEvent.StatusUpdated -> {
                    snackbarHostState.showSnackbar(event.message)
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
            val activeApp = state.activeApplication
            if (offer != null) {
                Surface(color = White, shadowElevation = 8.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.isActionLoading) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Teal500)
                            }
                        } else if (activeApp == null) {
                            PrimaryButton(
                                text = "Postular a esta oferta",
                                onClick = { showSheet = true }
                            )
                        } else {
                            // Flow options based on status
                            when (activeApp.rawStatus) {
                                "PENDIENTE", "ACEPTADO" -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            SecondaryButton(
                                                text = if (activeApp.rawStatus == "ACEPTADO") "Seleccionado (Espera Pago)" else "Postulado",
                                                onClick = {},
                                                enabled = false
                                            )
                                        }
                                        androidx.compose.material3.OutlinedButton(
                                            onClick = {
                                                activeApp.id.toLongOrNull()?.let { appId ->
                                                    viewModel.cancelPostulation(appId)
                                                }
                                            },
                                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                                contentColor = androidx.compose.ui.graphics.Color.Red
                                            ),
                                            border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color.Red),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(48.dp)
                                        ) {
                                            Text("Cancelar", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                "PAGO_ENVIADO" -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                                    ) {
                                        androidx.compose.material3.Button(
                                            onClick = {
                                                activeApp.id.toLongOrNull()?.let { appId ->
                                                    viewModel.updateApplicationStatus(appId, "PAGO_CONFIRMADO")
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                                containerColor = androidx.compose.ui.graphics.Color(0xFF059669)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Confirmar Recepción de Pago ✅", color = White, fontWeight = FontWeight.Bold)
                                        }
                                        androidx.compose.material3.TextButton(
                                            onClick = { showDisputeDialog = true },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("No he recibido el pago / Reportar ⚠️", color = androidx.compose.ui.graphics.Color.Red, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                "PAGO_DISPUTADO" -> {
                                    androidx.compose.material3.Button(
                                        onClick = {},
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = androidx.compose.ui.graphics.Color(0xFFD97706)
                                        ),
                                        enabled = false,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("En Revisión de Admin ⏳", color = White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                "PAGO_CONFIRMADO", "CALIFICADO_EMPRESA" -> {
                                    androidx.compose.material3.Button(
                                        onClick = { showRatingDialog = true },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = androidx.compose.ui.graphics.Color(0xFFFFB300)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Calificar Empresa ⭐", color = White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                "FINALIZADA", "CALIFICADO_TRABAJADOR" -> {
                                    androidx.compose.material3.Button(
                                        onClick = {},
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = androidx.compose.ui.graphics.Color(0xFF059669)
                                        ),
                                        enabled = false,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Trabajo Completado y Calificado 🎉", color = White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                else -> {
                                    SecondaryButton(
                                        text = activeApp.rawStatus.lowercase().capitalize(),
                                        onClick = {},
                                        enabled = false
                                    )
                                }
                            }
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
                activeApplication = state.activeApplication,
                onShowDistanceClick = { showDistanceDialog = true },
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

    if (showDistanceDialog && state.offer?.distanceKm != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDistanceDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Teal500,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Distancia al Empleo 📍", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Estás a una distancia aproximada de:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700
                    )
                    Text(
                        text = "${String.format("%.1f", state.offer!!.distanceKm!!)} kilómetros",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Teal500
                        )
                    )
                    Text(
                        text = "Esta distancia se calcula en línea recta desde tu dirección registrada hasta la ubicación del empleo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showDistanceDialog = false }
                ) {
                    Text("Entendido", color = Teal500, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showRatingDialog && state.activeApplication != null) {
        var stars by remember { mutableStateOf(5) }
        var feedback by remember { mutableStateOf("") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = {
                Text(
                    text = "Calificar Empresa ⭐",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¿Cómo fue tu experiencia trabajando con ${state.offer?.company ?: "la empresa"}?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700
                    )
                    
                    // Star selector
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        (1..5).forEach { index ->
                            IconButton(onClick = { stars = index }) {
                                Icon(
                                    imageVector = if (index <= stars) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "$index estrellas",
                                    tint = if (index <= stars) androidx.compose.ui.graphics.Color(0xFFFFB300) else Gray500,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                    
                    // Feedback text field
                    androidx.compose.material3.OutlinedTextField(
                        value = feedback,
                        onValueChange = { feedback = it },
                        label = { Text("Comentario u opinión (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal500,
                            focusedLabelColor = Teal500
                        )
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        val appId = state.activeApplication!!.id.toLongOrNull()
                        if (appId != null) {
                            val nextStatus = if (state.activeApplication!!.rawStatus == "CALIFICADO_EMPRESA") "FINALIZADA" else "CALIFICADO_TRABAJADOR"
                            viewModel.updateApplicationStatus(appId, nextStatus)
                        }
                        showRatingDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Teal500)
                ) {
                    Text("Enviar Calificación", color = White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showRatingDialog = false }) {
                    Text("Cancelar", color = Gray500)
                }
            },
            containerColor = White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDisputeDialog && state.activeApplication != null) {
        var reason by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDisputeDialog = false },
            title = {
                Text(
                    text = "Reportar Problema con Pago ⚠️",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Describe el problema con el pago (ej: no se ve el comprobante, el monto no coincide, no he recibido la transferencia). Esto alertará a un administrador.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700
                    )
                    
                    androidx.compose.material3.OutlinedTextField(
                        value = reason,
                        onValueChange = { 
                            reason = it
                            isError = false
                        },
                        label = { Text("Motivo del reporte") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        isError = isError,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = androidx.compose.ui.graphics.Color.Red,
                            focusedLabelColor = androidx.compose.ui.graphics.Color.Red
                        )
                    )
                    if (isError) {
                        Text(
                            text = "Debes ingresar un motivo para reportar.",
                            color = androidx.compose.ui.graphics.Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        if (reason.trim().isBlank()) {
                            isError = true
                        } else {
                            val appId = state.activeApplication!!.id.toLongOrNull()
                            if (appId != null) {
                                viewModel.updateApplicationStatus(appId, "PAGO_DISPUTADO")
                            }
                            showDisputeDialog = false
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Red)
                ) {
                    Text("Reportar Problema", color = White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDisputeDialog = false }) {
                    Text("Cancelar", color = Gray500)
                }
            },
            containerColor = White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OfferDetailContent(
    offer: JobOffer,
    activeApplication: cl.catchgo.app.domain.model.JobApplication?,
    onShowDistanceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Header(offer)

        activeApplication?.let { app ->
            val (bannerBg, bannerBorder, bannerText, bannerIcon, bannerDesc) = when (app.rawStatus) {
                "PENDIENTE" -> listOf(
                    BrandBlue50, BrandBlue700.copy(alpha = 0.3f), BrandBlue700, 
                    Icons.Outlined.Schedule, "Has postulado a este turno. Esperando selección de la empresa."
                )
                "ACEPTADO" -> listOf(
                    Teal50, Teal700.copy(alpha = 0.3f), Teal700, 
                    Icons.Outlined.CheckCircle, "¡Fuiste seleccionado! Realiza el turno según las fechas acordadas."
                )
                "RECHAZADO" -> listOf(
                    androidx.compose.ui.graphics.Color(0xFFFEF2F2), androidx.compose.ui.graphics.Color(0xFFFCA5A5), androidx.compose.ui.graphics.Color(0xFF991B1B), 
                    Icons.Outlined.Cancel, "Tu postulación no fue seleccionada esta vez."
                )
                "PAGO_ENVIADO" -> listOf(
                    Teal50, Teal700.copy(alpha = 0.3f), Teal700, 
                    Icons.Outlined.AttachMoney, "La empresa reportó el envío de tu pago. Por favor, revísalo y valídalo abajo."
                )
                "PAGO_DISPUTADO" -> listOf(
                    androidx.compose.ui.graphics.Color(0xFFFFFBEB), androidx.compose.ui.graphics.Color(0xFFFDE68A), androidx.compose.ui.graphics.Color(0xFF92400E), 
                    Icons.Outlined.Warning, "Reportaste una discrepancia con el pago. Un administrador revisará tu caso."
                )
                "PAGO_CONFIRMADO", "CALIFICADO_EMPRESA" -> listOf(
                    Teal50, Teal700.copy(alpha = 0.3f), Teal700, 
                    Icons.Outlined.Star, "Pago recibido con éxito. Por favor califica a la empresa para finalizar."
                )
                "FINALIZADA", "CALIFICADO_TRABAJADOR" -> listOf(
                    Teal50, Teal700.copy(alpha = 0.3f), Teal700, 
                    Icons.Outlined.CheckCircle, "¡Turno completado y calificado con éxito! Gracias por tu trabajo."
                )
                else -> listOf(
                    Gray100, Gray200, Gray700, 
                    Icons.Outlined.WorkOutline, "Estado: ${app.rawStatus}"
                )
            }
            
            Surface(
                color = bannerBg as androidx.compose.ui.graphics.Color,
                border = BorderStroke(1.dp, bannerBorder as androidx.compose.ui.graphics.Color),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Icon(
                        imageVector = bannerIcon as ImageVector,
                        contentDescription = null,
                        tint = bannerText as androidx.compose.ui.graphics.Color,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Estado de tu Postulación",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = bannerText
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = bannerDesc as String,
                            style = MaterialTheme.typography.bodySmall,
                            color = bannerText.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            SalaryCard(offer.salaryClp, offer.salaryUnit)

            HorizontalDivider(color = Gray200)

            SectionTitle("Sobre el trabajo")
            Text(
                text = offer.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray700,
                lineHeight = 22.sp
            )

            SectionTitle("Detalles")
            DetailItem(icon = Icons.Outlined.LocationOn, label = "${offer.comuna}, ${offer.region}")
            DetailItem(icon = Icons.Outlined.WorkOutline, label = offer.jornada)
            DetailItem(icon = Icons.Outlined.Schedule, label = offer.scheduleText)

            if (offer.distanceKm != null) {
                Spacer(Modifier.height(Spacing.xs))
                androidx.compose.material3.TextButton(
                    onClick = onShowDistanceClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        containerColor = Teal50,
                        contentColor = Teal700
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Teal700
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Calcular distancia entre ambos: ${String.format("%.1f", offer.distanceKm)} km",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Teal700
                    )
                }
            }

            if (offer.requirements.isNotEmpty()) {
                SectionTitle("Requisitos")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    offer.requirements.forEach { req ->
                        RequirementChip(req)
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun Header(offer: JobOffer) {
    val (avatarBg, avatarFg) = listOf(
        Teal50 to Teal700,
        BrandBlue50 to BrandBlue700
    )[offer.company.hashCode().and(0x7FFFFFFF) % 2]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(avatarBg),
                contentAlignment = Alignment.Center
            ) {
                if (!offer.photoUrl.isNullOrBlank()) {
                    val mappedUrl = offer.photoUrl
                        .replace("localhost", "10.0.2.2")
                        .replace("127.0.0.1", "10.0.2.2")
                    AsyncImage(
                        model = mappedUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = offer.company.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = avatarFg
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = offer.company,
                    style = MaterialTheme.typography.labelLarge,
                    color = Gray500
                )
                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Gray900
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(text = offer.category.display, type = StatusType.Info)
            StatusBadge(text = offer.comuna, type = StatusType.Neutral)
            Spacer(Modifier.weight(1f))
            ScoreBadge(offer.score)
        }
    }
}

@Composable
private fun SalaryCard(salaryClp: Int, unit: String) {
    val formatted = "%,d".format(salaryClp).replace(",", ".")
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Teal50,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Icon(
                imageVector = Icons.Outlined.AttachMoney,
                contentDescription = null,
                tint = Teal700,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = "\$$formatted CLP",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Teal700
                )
                Text(
                    text = "por $unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = Teal500
                )
            }
        }
    }
}

@Composable
private fun ScoreBadge(score: Int) {
    val (bg, fg) = when {
        score >= 80 -> Teal50 to Teal700
        score >= 60 -> BrandBlue50 to BrandBlue700
        else -> Gray100 to Gray700
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = "$score% match",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = fg
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = Gray900,
        modifier = Modifier.padding(top = Spacing.xs)
    )
}

@Composable
private fun DetailItem(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Teal500, modifier = Modifier.size(18.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Gray700)
    }
}

@Composable
private fun RequirementChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(BrandBlue50)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = BrandBlue700
        )
    }
}

