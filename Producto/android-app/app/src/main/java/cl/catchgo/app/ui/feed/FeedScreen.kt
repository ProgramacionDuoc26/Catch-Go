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

@Composable
fun FeedScreen(
    role: UserRole,
    onOfferClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (role) {
        UserRole.WORKER -> WorkerFeedRoute(onOfferClick = onOfferClick, modifier = modifier)
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
            onOfferClick = onOfferClick
        )
    }
}

@Composable
private fun FeedContent(
    state: FeedUiState,
    onCategorySelect: (JobCategory?) -> Unit,
    onSearchChange: (String) -> Unit,
    onOfferClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        FeedHeader(
            count = if (state.isLoading) null else state.offers.size,
            searchQuery = state.searchQuery,
            onSearchChange = onSearchChange
        )
        CategoryChipsRow(
            selected = state.selectedCategory,
            onSelect = onCategorySelect
        )
        when {
            state.isLoading -> SkeletonList()
            state.offers.isEmpty() -> EmptyFeed(
                filteredBy = state.selectedCategory,
                hasQuery = state.searchQuery.isNotBlank()
            )
            else -> OffersList(offers = state.offers, onOfferClick = onOfferClick)
        }
    }
}

@Composable
private fun FeedHeader(count: Int?, searchQuery: String, onSearchChange: (String) -> Unit) {
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
        Text(
            text = "Ofertas para ti",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = White
        )
        if (count != null) {
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
