package cl.catchgo.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.catchgo.app.domain.model.JobCategory
import cl.catchgo.app.domain.model.JobOffer
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.ui.components.EmptyState
import cl.catchgo.app.ui.components.OfferCard
import cl.catchgo.app.ui.empresa.EmpresaOfertasScreen
import cl.catchgo.app.ui.components.SkeletonBox
import cl.catchgo.app.ui.theme.BrandBlue50
import cl.catchgo.app.ui.theme.BrandBlue600
import cl.catchgo.app.ui.theme.BrandBlue700
import cl.catchgo.app.ui.theme.Gray200
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Navy
import cl.catchgo.app.ui.theme.NavyDeep
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.Teal50
import cl.catchgo.app.ui.theme.Teal500
import cl.catchgo.app.ui.theme.White
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.material3.Surface
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import cl.catchgo.app.ui.components.StatusBadge
import cl.catchgo.app.ui.components.StatusType
import cl.catchgo.app.ui.theme.Gray700
import cl.catchgo.app.ui.theme.Gray200
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Assignment
import cl.catchgo.app.domain.model.JobApplication

@Composable
fun FeedScreen(
    role: UserRole,
    onOfferClick: (String) -> Unit = {},
    onCrearOferta: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (role) {
        UserRole.WORKER -> WorkerFeedRoute(onOfferClick = onOfferClick, modifier = modifier)
        UserRole.EMPRESA -> EmpresaOfertasScreen(onCrearOferta = onCrearOferta, modifier = modifier)
        else -> FeedPlaceholderScreen(role = role, modifier = modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerFeedRoute(
    onOfferClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        FeedContent(
            state = state,
            onCategorySelect = viewModel::onCategorySelect,
            onSearchChange = viewModel::onSearchChange,
            onTabSelect = viewModel::onTabSelect,
            onOfferClick = onOfferClick
        )
    }
}

@Composable
private fun FeedContent(
    state: FeedUiState,
    onCategorySelect: (JobCategory?) -> Unit,
    onSearchChange: (String) -> Unit,
    onTabSelect: (FeedTab) -> Unit,
    onOfferClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        FeedHeader(
            count = if (state.isLoading) null else {
                if (state.activeTab == FeedTab.EXPLORAR) state.offers.size
                else state.filteredApplications.size
            },
            searchQuery = state.searchQuery,
            profileCompletion = state.profileCompletion,
            onSearchChange = onSearchChange
        )
        
        // Horizontal Scrollable Tab Bar
        DashboardTabBar(
            activeTab = state.activeTab,
            postCount = state.postulacionesCount,
            pagosCount = state.pagosCount,
            porCalifCount = state.porCalificarCount,
            compCount = state.completadasCount,
            onTabSelect = onTabSelect
        )

        // Show category chips ONLY if we are in "EXPLORAR" tab!
        if (state.activeTab == FeedTab.EXPLORAR) {
            CategoryChipsRow(
                selected = state.selectedCategory,
                onSelect = onCategorySelect
            )
        }

        when {
            state.isLoading -> SkeletonList()
            state.activeTab == FeedTab.EXPLORAR -> {
                if (state.offers.isEmpty()) {
                    EmptyFeed(
                        filteredBy = state.selectedCategory,
                        hasQuery = state.searchQuery.isNotBlank()
                    )
                } else {
                    OffersList(offers = state.offers, onOfferClick = onOfferClick)
                }
            }
            else -> {
                if (state.filteredApplications.isEmpty()) {
                    EmptyApplicationsState(hasQuery = state.searchQuery.isNotBlank())
                } else {
                    ApplicationsList(
                        applications = state.filteredApplications,
                        onApplicationClick = { offerId -> onOfferClick(offerId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedHeader(
    count: Int?,
    searchQuery: String,
    profileCompletion: Int,
    onSearchChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Navy, BrandBlue600),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
            .padding(horizontal = Spacing.lg, vertical = Spacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ofertas Disponibles",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = White
                )
                Text(
                    text = "Encuentra turnos que calcen con tu perfil y ubicación.",
                    style = MaterialTheme.typography.bodySmall,
                    color = White.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            // Profile Completion Box (Glassmorphic)
            Surface(
                color = White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text(
                        text = "TU PERFIL:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = White.copy(alpha = 0.9f)
                    )
                    // Linear progress bar in premium styling
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(White.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = profileCompletion.toFloat() / 100f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(White)
                        )
                    }
                    Text(
                        text = "$profileCompletion%",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = White
                    )
                }
            }
        }
        
        if (count != null) {
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = if (count == 1) "1 oferta compatible" else "$count ofertas compatibles",
                style = MaterialTheme.typography.bodySmall,
                color = White.copy(alpha = 0.75f)
            )
        }

        Box(modifier = Modifier.padding(top = Spacing.md)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Buscar por cargo, empresa o comuna…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = Gray500,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedBorderColor = Teal500,
                    unfocusedBorderColor = Gray200,
                    cursorColor = BrandBlue600
                ),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChipsRow(
    selected: JobCategory?,
    onSelect: (JobCategory?) -> Unit
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = BrandBlue50,
        selectedLabelColor = BrandBlue700
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm)
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("Todas") },
                colors = chipColors
            )
        }
        items(JobCategory.entries) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(if (selected == category) null else category) },
                label = { Text(category.display) },
                colors = chipColors
            )
        }
    }
}

@Composable
private fun OffersList(
    offers: List<JobOffer>,
    onOfferClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.lg,
            end = Spacing.lg,
            top = Spacing.xs,
            bottom = Spacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items(items = offers, key = { it.id }) { offer ->
            val salaryText = "\$${"%,d".format(offer.salaryClp).replace(",", ".")} / ${offer.salaryUnit}"
            OfferCard(
                titulo = offer.title,
                empresa = offer.company,
                comuna = offer.comuna,
                jornada = offer.jornada,
                score = offer.score,
                salaryText = salaryText,
                distanceKm = offer.distanceKm,
                photoUrl = offer.photoUrl,
                onClick = { onOfferClick(offer.id) }
            )
        }
    }
}

@Composable
private fun SkeletonList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        repeat(4) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = MaterialTheme.shapes.large
            )
        }
    }
}

@Composable
private fun EmptyFeed(filteredBy: JobCategory?, hasQuery: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyState(
            title = when {
                hasQuery -> "Sin resultados"
                filteredBy != null -> "Sin ofertas en ${filteredBy.display}"
                else -> "No hay ofertas disponibles"
            },
            description = when {
                hasQuery -> "Intenta con otro término de búsqueda."
                filteredBy != null -> "Prueba con otra categoría o quita el filtro."
                else -> "Pronto habrá nuevas oportunidades. Desliza hacia abajo para refrescar."
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.SearchOff,
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(40.dp)
                )
            }
        )
    }
}

@Composable
private fun DashboardTabBar(
    activeTab: FeedTab,
    postCount: Int,
    pagosCount: Int,
    porCalifCount: Int,
    compCount: Int,
    onTabSelect: (FeedTab) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = Spacing.xs)
    ) {
        item {
            TabButton(
                title = "Explorar Ofertas",
                count = null,
                icon = Icons.Outlined.Search,
                activeColor = BrandBlue600,
                selected = activeTab == FeedTab.EXPLORAR,
                onClick = { onTabSelect(FeedTab.EXPLORAR) }
            )
        }
        item {
            TabButton(
                title = "Postulaciones",
                count = postCount,
                icon = Icons.Outlined.CheckCircle,
                activeColor = BrandBlue600,
                selected = activeTab == FeedTab.POSTULACIONES,
                onClick = { onTabSelect(FeedTab.POSTULACIONES) }
            )
        }
        item {
            TabButton(
                title = "Pagos",
                count = pagosCount,
                icon = Icons.Outlined.AttachMoney,
                activeColor = androidx.compose.ui.graphics.Color(0xFF10B981), // Emerald Teal
                selected = activeTab == FeedTab.PAGOS,
                onClick = { onTabSelect(FeedTab.PAGOS) }
            )
        }
        item {
            TabButton(
                title = "Por Calificar",
                count = porCalifCount,
                icon = Icons.Outlined.Star,
                activeColor = androidx.compose.ui.graphics.Color(0xFFF59E0B), // Golden Amber
                selected = activeTab == FeedTab.POR_CALIFICAR,
                onClick = { onTabSelect(FeedTab.POR_CALIFICAR) }
            )
        }
        item {
            TabButton(
                title = "Completadas",
                count = compCount,
                icon = Icons.Outlined.Assignment,
                activeColor = Gray700,
                selected = activeTab == FeedTab.COMPLETADAS,
                onClick = { onTabSelect(FeedTab.COMPLETADAS) }
            )
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    count: Int?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    activeColor: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (selected) activeColor else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) White else Gray500,
        shape = RoundedCornerShape(12.dp),
        border = if (selected) null else BorderStroke(1.dp, Gray200),
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (selected) White else Gray500
            )
            Text(
                text = if (count != null) "$title ($count)" else title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun ApplicationsList(
    applications: List<JobApplication>,
    onApplicationClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.lg,
            end = Spacing.lg,
            top = Spacing.xs,
            bottom = Spacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items(items = applications, key = { it.id }) { application ->
            ApplicationCard(
                application = application,
                onClick = { onApplicationClick(application.offerId) }
            )
        }
    }
}

private val avatarColors = listOf(
    Color(0xFF0E7490) to Color(0xFFECFEFF),
    Color(0xFF1D4ED8) to Color(0xFFEFF6FF),
    Color(0xFF7C3AED) to Color(0xFFF5F3FF),
    Color(0xFF0F766E) to Color(0xFFF0FDFA),
    Color(0xFFB45309) to Color(0xFFFFFBEB),
    Color(0xFF9D174D) to Color(0xFFFFF1F2),
)

@Composable
private fun ApplicationCard(
    application: JobApplication,
    onClick: () -> Unit
) {
    val colorPair = avatarColors[application.company.hashCode().and(0x7FFFFFFF) % avatarColors.size]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Gray200),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colorPair.second),
                    contentAlignment = Alignment.Center
                ) {
                    if (!application.photoUrl.isNullOrBlank()) {
                        val mappedUrl = application.photoUrl
                            .replace("localhost", cl.catchgo.app.data.remote.ApiConfig.HOST)
                            .replace("127.0.0.1", cl.catchgo.app.data.remote.ApiConfig.HOST)
                        AsyncImage(
                            model = mappedUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = application.company.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorPair.first
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.offerTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${application.company} · ${application.comuna}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                }
                
                val display = when (application.rawStatus) {
                    "PENDIENTE" -> "Pendiente"
                    "ACEPTADO" -> "Aceptada"
                    "RECHAZADO" -> "Rechazada"
                    "TRABAJO_FINALIZADO" -> "Trabajo Finalizado"
                    "PAGO_ENVIADO" -> "Pago Enviado"
                    "PAGO_DISPUTADO" -> "Pago en Disputa"
                    "PAGO_CONFIRMADO" -> "Pago Confirmado"
                    "CALIFICADO_EMPRESA" -> "Calificada"
                    "CALIFICADO_TRABAJADOR" -> "Finalizada"
                    "FINALIZADA" -> "Finalizada"
                    "ARCHIVADA" -> "Archivada"
                    else -> application.rawStatus.lowercase().replaceFirstChar { it.uppercase() }
                }
                
                val type = when (application.rawStatus) {
                    "ACEPTADO", "PAGO_CONFIRMADO", "FINALIZADA", "CALIFICADO_TRABAJADOR" -> StatusType.Success
                    "RECHAZADO", "PAGO_DISPUTADO" -> StatusType.Error
                    else -> StatusType.Pending
                }
                
                StatusBadge(
                    text = display,
                    type = type
                )
            }
            Text(
                text = cl.catchgo.app.core.util.timeAgo(application.createdAtIso),
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
            if (!application.message.isNullOrBlank()) {
                Text(
                    text = "“${application.message}”",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray700
                )
            }
        }
    }
}

@Composable
private fun EmptyApplicationsState(hasQuery: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyState(
            title = if (hasQuery) "Sin resultados" else "No tienes postulaciones en esta categoría",
            description = if (hasQuery) "Intenta con otro término de búsqueda." else "Las postulaciones que cambien a este estado aparecerán aquí.",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.SearchOff,
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(40.dp)
                )
            }
        )
    }
}

